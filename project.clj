(defproject org.domaindrivenarchitecture/httpd "0.2.1-SNAPSHOT"
  :description "Pallet crate to install and run Apache httpd"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.palletops/pallet "0.8.12"]
                 [com.palletops/git-crate "0.8.0-alpha.2" :exclusions [org.clojure/clojure]]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 [com.palletops/pallet-vmfest "0.4.0-alpha.1"]]
  :repositories [["snapshots" :clojars]
                 ["releases" :clojars]]
  :deploy-repositories [["snapshots" :clojars]
                        ["releases" :clojars]]
  :profiles {:dev
             {:dependencies
              [[com.palletops/pallet "0.8.12" :classifier "tests"]]
              :plugins
              [[com.palletops/pallet-lein "0.8.0-alpha.1"]]}
             :aws 
             {:dependencies 
              [
               [com.palletops/pallet-jclouds "1.7.3"]
               ;; To get started we include all jclouds compute providers.
               ;; You may wish to replace this with the specific jclouds
               ;; providers you use, to reduce dependency sizes.
               [org.apache.jclouds/jclouds-allblobstore "1.9.2"]
               [org.apache.jclouds/jclouds-allcompute "1.9.2"]
               [org.apache.jclouds.driver/jclouds-slf4j "1.9.2"
                ;; the declared version is old and can overrule the
                ;; resolved version
                :exclusions [org.slf4j/slf4j-api]]
               [org.apache.jclouds.driver/jclouds-sshj "1.9.2"]]}
             :leiningen/reply
             {:dependencies [[org.slf4j/jcl-over-slf4j "1.7.21"]]
              :exclusions [commons-logging]}}
  :local-repo-classpath true
  )
