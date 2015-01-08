(ns httpd.groups.httpd
    "Node defintions for httpd"
    (:require
     [pallet.api :refer [group-spec server-spec node-spec plan-fn]]
     [pallet.crate.automated-admin-user :refer [automated-admin-user]]
     [httpd.crate.httpd :as httpd]))

(def default-node-spec
  (node-spec
   :image {:image-id :ubuntu-14.04}
   ;;:image {:os-family :ubuntu}
   :hardware {:min-cores 1}))

(def
  ^{:doc "Defines the type of node httpd will run on"}
  base-server
  (server-spec
   :phases
   {:bootstrap (plan-fn (automated-admin-user))}))

(def
  ^{:doc "Define a server spec for httpd"}
  httpd-server
  (server-spec
   :phases
   {:configure (plan-fn
                ;; in addition to installing apache (which we get for
                ;; free by extending the httpd/server-spec in our
                ;; group-spec) we also want to set up a virtualhost
                (httpd/install-vhost "all2.us"
                 {:server-admin-email "upgradingdave@gmail.com"
                  :document-root-path "/home/dparoulek/apps/all2.us"
                  :port "3000"})
                ;; our virtual host requires a few mods so lets set
                ;; those up as well
                (httpd/a2enmod "rewrite")
                (httpd/a2enmod "proxy_http")
                ;; In order to activate all this, lets restart the server
                (httpd/apache2ctl "restart"))}))

(def
  ^{:doc "Defines a group spec that can be passed to converge or lift."}
  httpd
  (group-spec
   "httpd"
   :extends [base-server 
             ;; by extending httpd/server-spec, apache will be
             ;; installed during configuration phase
             (httpd/server-spec {})
             ;; in addition to installing apache, we want to set up a
             ;; vhost during the config phase. This is set up in the
             ;; httpd-server fn above, so we add that here as well.
             httpd-server
]
   :node-spec default-node-spec))
