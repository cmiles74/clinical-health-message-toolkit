(ns build
  "The build script for the Polylith project.

   Primary targets:
   * jar :project PROJECT
     - creates a library JAR for the given project
   * uberjar :project PROJECT
     - creates an uberjar for the given project

   For help, run:

   clojure -A:deps -T:build help/doc"
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.build.api :as b]
            [clojure.tools.deps :as t]
            [clojure.tools.deps.util.dir :refer [with-dir]]
            [deps-deploy.deps-deploy :as d]))

(defn- get-project-aliases []
  (let [edn-fn (juxt :root-edn :project-edn)]
    (-> (t/find-edn-maps)
        (edn-fn)
        (t/merge-edns)
        :aliases)))

(defn- ensure-project-root
  "Given a task name and a project name, ensure the project
   exists and seems valid, and return the absolute path to it."
  [task project]
  (let [project-root (str (System/getProperty "user.dir") "/projects/" project)]
    (when-not (and project
                   (.exists (io/file project-root))
                   (.exists (io/file (str project-root "/deps.edn"))))
      (throw (ex-info (str task " task requires a valid :project option") {:project project})))
    project-root))

(defn- lifted-basis
  "This creates a basis where source deps have their primary
   external dependencies lifted to the top-level, such as is
   needed by Polylith and possibly other monorepo setups."
  []
  (let [default-libs (:libs (b/create-basis))
        source-dep? #(not (:mvn/version (get default-libs %)))
        lifted-deps
        (reduce-kv (fn [deps lib {:keys [dependents] :as coords}]
                     (if (and (contains? coords :mvn/version) (some source-dep? dependents))
                       (assoc deps lib (select-keys coords [:mvn/version :exclusions]))
                       deps))
                   {}
                   default-libs)]
    (-> (b/create-basis {:extra {:deps lifted-deps}})
        (update :libs #(into {} (filter (comp :mvn/version val)) %)))))

(defn clean
  "Cleans the specified project by deleting the 'target' directory and it's
  contents.

  Options:
  * :project: - required, the name of the project to clean "
  [{:keys [project] :as opts}]
  (let [project-root (ensure-project-root "clean" project)]
    (println "Cleaning" project-root)
    (b/delete {:path (str project-root "/target")})))

(defn jar
  "Builds a library jar for the specified project.

   Options:
   * :project - required, the name of the project to build,
   * :jar-file - optional, the path of the JAR file to build,
     relative to the project folder; can also be specified in
     the :jar alias in the project's deps.edn file; will
     default to target/PROJECT-thin.jar if not specified.

   Returns:
   * the input opts with :class-dir, :jar-file, :lib, :pom-file,
     and :version computed.

   Because we build JARs from Polylith projects, all the source
   code we want in the JAR comes from :local/root dependencies of
   the project and the actual dependencies are transitive to those
   :local/root dependencies, so we create a 'lifted' basis.

   Example: clojure -T:build jar :project project-name"
  [{:keys [project jar-file] :as opts}]
  (let [project-root (ensure-project-root "jar" project)
        root-aliases (with-dir (io/file ".") (get-project-aliases))
        aliases (with-dir (io/file project-root) (get-project-aliases))]
    (println "Building library jar for" project-root)
    (b/with-project-root project-root
      (let [basis (lifted-basis)
            class-dir "target/classes"
            lib (symbol (str (get-in root-aliases [:deploy :group]) "/"
                             project))
            licenses (get-in root-aliases [:deploy :licenses])
            current-version (get-in aliases [:library :version])
            snapshot? (if (string/ends-with? current-version
                                             "-SNAPSHOT")
                        true false)
            jar-file (or jar-file
                         (-> aliases :jar :jar-file)
                         (str "target/" project ".jar"))
            current-dir (System/getProperty "user.dir")
            current-rel #(string/replace % (str current-dir "/") "")
            directory? #(let [f (java.io.File. %)]
                          (and (.exists f) (.isDirectory f)))
            src+dirs (filter directory? (:classpath-roots basis))
            opts (merge opts
                        {:basis basis
                         :class-dir class-dir
                         :lib lib
                         :jar-file jar-file
                         :scm {:tag (if snapshot?
                                      "SNAPSHOT"
                                      (str "v" current-version))
                               :name "git"
                               :url "https://github.com/cmiles74/clinical-health-message-toolkit"}
                         :src-pom "partial_pom.xml"
                         :version current-version
                         :pom-data licenses})]
        (b/delete {:path class-dir})
        (println "\nWriting pom.xml..." lib current-version)
        (b/write-pom (merge opts {:target (str project-root "/target")
                                  :class-dir nil}))
        (println "Copying" (str (string/join ", " (map current-rel src+dirs)) "..."))
        (b/copy-dir {:src-dirs src+dirs
                     :target-dir class-dir})
        (println "Building jar" (str jar-file "..."))
        (b/jar opts)
        (println "Jar is built.")
        (-> opts
            (assoc :pom-file (str project-root "/pom.xml"))
            ;; account for project root relative paths:
            (update :jar-file (comp #(.getCanonicalPath %) b/resolve-path)))))))

(defn uberjar
  "Builds an uberjar for the specified project.

   Options:
   * :project - required, the name of the project to build,
   * :uber-file - optional, the path of the JAR file to build,
     relative to the project folder; can also be specified in
     the :uberjar alias in the project's deps.edn file; will
     default to target/PROJECT.jar if not specified.

   Returns:
   * the input opts with :class-dir, :compile-opts, :main, and :uber-file
     computed.

   The project's deps.edn file must contain an :uberjar alias
   which must contain at least :main, specifying the main ns
   (to compile and to invoke)."
  [{:keys [project uber-file] :as opts}]
  (let [project-root (ensure-project-root "uberjar" project)
        aliases (with-dir (io/file project-root) (get-project-aliases))
        main (-> aliases :uberjar :main)]
    (println "Building uberjar for" project-root)
    (when-not main
      (throw (ex-info (str "the " project " project's deps.edn file does not specify the :main namespace in its :uberjar alias")
                      {:aliases aliases})))
    (b/with-project-root project-root
      (let [class-dir "target/classes"
            uber-file (or uber-file
                          (-> aliases :uberjar :uber-file)
                          (str "target/" project ".jar"))
            opts (merge opts
                        {:basis (b/create-basis)
                         :class-dir class-dir
                         :compile-opts {:direct-linking true}
                         :main main
                         :ns-compile [main]
                         :uber-file uber-file
                         :exclude [#"(?i)^META-INF/license/.*"
                                   #"^license/.*"]})]
        (b/delete {:path class-dir})
        (println "\nCompiling" (str main "..."))
        (b/compile-clj opts)
        (println "Building uberjar" (str uber-file "..."))
        (b/uber opts)
        (b/delete {:path class-dir})
        (println "Uberjar is built.")
        opts))))

(defn deploy
  "Deploys the jar file for the specified project.

   Options:
   * :project - required, the name of the project to build,"
  [{:keys [project] :as opts}]
  (let [project-root (ensure-project-root "deploy" project)
        target (str project-root "/target/" project ".jar")
        pom-file (str project-root "/target/pom.xml")
        deploy-opts {:installer :remote
                     :sign-releases? true
                     :artifact target
                     :pom-file pom-file}]
    (println "Deploying jar for" project-root)
    (d/deploy deploy-opts)))
