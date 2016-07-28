; Copyright (c) Michael Jerger. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(ns httpd.crate.mod-jk-test
  (:require
    [clojure.test :refer :all]
    [pallet.actions :as actions]
    [httpd.crate.mod-jk :as sut]
    ))

(deftest test-workers-configuration
  (testing
    "tests the modjk central config"
    (is 
      (= ["worker.list=mod_jk_www" 
          "worker.maintain=90" 
          "worker.mod_jk_www.port=8009" 
          "worker.mod_jk_www.host=127.0.0.1" 
          "worker.mod_jk_www.type=ajp13" 
          "worker.mod_jk_www.socket_connect_timeout=900000" 
          "worker.mod_jk_www.socket_keepalive=false" 
          "worker.mod_jk_www.connection_pool_timeout=100" 
          ""]
         (sut/workers-configuration)))
    (is 
      (= ["JkWorkerProperty worker.list=mod_jk_www" 
          "JkWorkerProperty worker.maintain=90" 
          "JkWorkerProperty worker.mod_jk_www.port=8009" 
          "JkWorkerProperty worker.mod_jk_www.host=127.0.0.1" 
          "JkWorkerProperty worker.mod_jk_www.type=ajp13" 
          "JkWorkerProperty worker.mod_jk_www.socket_connect_timeout=900000"
          "JkWorkerProperty worker.mod_jk_www.ping_mode=I"
          "JkWorkerProperty worker.mod_jk_www.socket_keepalive=true" 
          "JkWorkerProperty worker.mod_jk_www.connection_pool_timeout=100" 
          ""]
         (sut/workers-configuration
           :in-httpd-conf true
           :socket-keep-alive true
           :ping-mode "I")))
    
    ))

(deftest test-modjk-conf
  (testing
    "tests the modjk central config"
    (is 
      (= ["<IfModule jk_module>" 
          "  "
          "  JkWorkersFile /etc/libapache2-mod-jk/workers.properties"
          "  "
          "  JkLogFile /var/log/apache2/mod_jk.log" 
          "  JkLogLevel info" 
          "  JkShmFile /var/log/apache2/jk-runtime-status" 
          "  " 
          "  JkOptions +RejectUnsafeURI" 
          "  JkStripSession On" 
          "  JkWatchdogInterval 120" 
          "  " 
          "</IfModule>"]
         (sut/mod-jk-configuration)))
    (is 
      (= ["<IfModule jk_module>" 
          "  "
          "  JkLogFile /var/log/apache2/mod_jk.log" 
          "  JkLogLevel info" 
          "  JkShmFile /var/log/apache2/jk-runtime-status" 
          "  " 
          "  JkOptions +RejectUnsafeURI" 
          "  JkStripSession On" 
          "  JkWatchdogInterval 120" 
          "  " 
          "</IfModule>"]
         (sut/mod-jk-configuration
           :workers-properties-file nil)))
    
    ))

(deftest test-vhost
  (testing
    "tests the vhost config"
    (is 
      (= ["JkMount /* mod_jk_www"]
         (sut/vhost-jk-mount
           :path "/*"
           :worker "mod_jk_www")))
    (is 
      (= ["JkUnMount /error/* mod_jk_www"]
         (sut/vhost-jk-unmount
           :path "/error/*"
           :worker "mod_jk_www")))
    ))