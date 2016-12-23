(defproject spec-examples "0.1.0-SNAPSHOT"
  :description "Spec for fun!" 
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies
                   [[org.clojure/tools.namespace "0.2.10"]
                    [org.clojure/test.check "0.9.0"]]

                   :plugins [[cider/cider-nrepl "0.15.0-snapshot"]]}})
