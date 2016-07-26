; Copyright (c) Michael Jerger. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(ns httpd.crate.mod-jk
  (require 
    [clojure.string :as string]
    [pallet.actions :as actions]
    [httpd.crate.cmds :as cmds]
    ))

(defn vhost-jk-mount
  [& {:keys [path worker]
      :or {path "/*"
           worker "mod_jk_www"}}]
  [(str "JkMount " path  " " worker)]
  )

(defn vhost-jk-unmount
  [& {:keys [path worker]
      :or {path "/*"
           worker "mod_jk_www"}}]
  [(str "JkUnMount " path  " " worker)]
  )

(defn vhost-jk-status-location
  []
  ["<Location /jk-status>"
   "  # Inside Location we can omit the URL in JkMount"
   "  JkMount jk-status"
   "  Order deny,allow"
   "  Deny from all"
   "  Allow from 127.0.0.1"
   "</Location>"
   "<Location /jk-manager>"
   "  # Inside Location we can omit the URL in JkMount"
   "  JkMount jk-manager"
   "  Order deny,allow"
   "  Deny from all"
   "  Allow from 127.0.0.1"
   "</Location>"
   ""])

(defn workers-configuration
  "Takes optional args and returns content for configure-mod-jk-worker"
   [& {:keys [worker host port socket-connect-timeout-ms maintain-timout-sec in-httpd-conf ping-mode socket-keep-alive]
      :or {port "8009"
           host "127.0.0.1"
           worker "mod_jk_www"
           socket-connect-timeout-ms 900000
           maintain-timout-sec 90
           in-httpd-conf false
           ping-mode nil
           socket-keep-alive false}}]
   (let [jkworkerproperty (when in-httpd-conf "JkWorkerProperty ")]
     (into 
       []
       (concat
         [(str jkworkerproperty "worker.list=" worker)
          (str jkworkerproperty "worker.maintain=" maintain-timout-sec)
          (str jkworkerproperty "worker." worker ".port=" port)
          (str jkworkerproperty "worker." worker ".host=" host)
          (str jkworkerproperty "worker." worker ".type=ajp13")
          (str jkworkerproperty "worker." worker ".socket_connect_timeout=" socket-connect-timeout-ms)]
         (when (some? ping-mode)
           [(str jkworkerproperty "worker." worker ".ping_mode=" ping-mode)])
         [(str jkworkerproperty "worker." worker ".socket_keepalive="socket-keep-alive)
          (str jkworkerproperty "worker." worker ".connection_pool_timeout=100")
          ""]))
     ))


(defn mod-jk-configuration
  "Takes optional args and generates a Vector of Strings"
  [&{:keys [jkStripSession jkWatchdogInterval vhost-jk-status-location? workers-properties-file]
     :or {jkStripSession "On"
          jkWatchdogInterval 120
          vhost-jk-status-location? false
          workers-properties-file "/etc/libapache2-mod-jk/workers.properties"}}]
  (into []
    (concat 
      ["<IfModule jk_module>"
       "  "]
       (when (some? workers-properties-file)
         [(str "  JkWorkersFile " workers-properties-file)
          "  "])
       ["  JkLogFile /var/log/apache2/mod_jk.log"
       "  JkLogLevel info"
       "  JkShmFile /var/log/apache2/jk-runtime-status"
       "  "
       "  JkOptions +RejectUnsafeURI"
       (str "  JkStripSession " jkStripSession)
       (str "  JkWatchdogInterval " jkWatchdogInterval)
       "  "]
      (when vhost-jk-status-location?
        (vhost-jk-status-location))
       ["</IfModule>"])))


(defn configure-mod-jk-worker
  "Takes optional args and creates a remote-file"
  [& {:keys [workers-configuration]
      :or {workers-configuration (workers-configuration)}}]
  (actions/remote-file
    "/etc/libapache2-mod-jk/workers.properties"
    :owner "root"
    :group "root"
    :mode "644"
    :force true
    :content 
    (string/join
      \newline
      workers-configuration)))

(defn install-mod-jk
  "Installs mod-jk and creates a remote file consisting of the mod-jk-configuration"
  [&{:keys [jkStripSession jkWatchdogInterval]
     :or {jkStripSession "On"
          jkWatchdogInterval 120}}]
  (actions/package "libapache2-mod-jk")
  (actions/remote-file
    "/etc/apache2/mods-available/jk.conf"
    :owner "root"
    :group "root"
    :mode "644"
    :force true
    :content 
    (string/join
      \newline
      (mod-jk-configuration jkStripSession jkWatchdogInterval)
      ))  
  (cmds/a2enmod "jk")
  )