(ns httpd.crate.httpd
  (:require [pallet.actions :refer [exec package]]
            [pallet.api :as api :refer [plan-fn]]
            [pallet.crate :refer [assoc-settings defplan
                                  get-settings]]
            [httpd.crate.config :as config]))

(def ^{:dynamic true} *default-settings*
  {})

(defplan settings
  "Set options for apache httpd install."
  [{:keys [instance-id] :as settings}]
  (assoc-settings
   :httpd (merge *default-settings* settings) {:instance-id instance-id}))

(defplan install 
  "Install apache2 package."
  [{:keys [instance-id]}]
  (let [settings (get-settings :httpd {:instance-id instance-id})]
    (package "apache2")))

(defn apache2ctl
  "Start, stop, restart with apache2ctl command"
  [cmd]
  (pallet.actions/exec nil (str "sudo apache2ctl " cmd)))

(defn a2enmod
  "Enable apache modules such as mod_rewrite, mode_proxy, etc"
  [mod]
  (pallet.actions/exec nil (str "a2enmod " mod)))

(defn server-spec
  "Options: (none yet)"
  [settings & {:keys [instance-id] :as options}]
  (api/server-spec
   :phases {:settings (plan-fn (httpd.crate.httpd/settings 
                                (merge settings options)))
            :bootstrap (plan-fn (install options))
            :restart (plan-fn (apache2ctl "restart"))}
   :default-phases [:install]))

(defn a2ensite
  "Enable vhost site. vhost-conf should be the name of a conf file in
  sites-available"
  [vhost-conf]
  (pallet.actions/exec nil (str "a2ensite " vhost-conf)))

(defn install-vhost 
  "Setup an Apache VirtualHost."
  [domain-name & [{:keys [server-admin-email document-root-path 
                    vhost-xml aliases port] :as opts}]]
  (let [content (config/vhost-conf domain-name opts)
        path (str "/etc/apache2/sites-available/" domain-name ".conf")]

    ;; create vhost conf file
    (pallet.actions/remote-file path 
                                :owner "root"
                                :group "root"
                                :mode "644"
                                :content content
                                :literal true
                                )
    ;; enable site
    (a2ensite (str domain-name ".conf"))))

(defn deploy-site
  [remote-dir-path]
  ;; ensure the directory is created
  (pallet.actions/directory remote-dir-path)
  ;; create index.html
  (let [content (str "<html>"\newline
                     "<head><title>New Site</title></head>"\newline
                     "<body><div>Hello, World</div></body>"\newline
                     "</html>")]
    (pallet.actions/remote-file (str remote-dir-path "/index.html")
                                :owner "root"
                                :group "root"
                                :mode "644"
                                :content content)))
