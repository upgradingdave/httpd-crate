; Copyright (c) Michael Jerger. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(ns httpd.crate.mod-proxy-http-test
  (:require
    [clojure.test :refer :all]
    [pallet.actions :as actions]
    [httpd.crate.mod-proxy-http :as sut]
    ))

(deftest vhost
  (testing
    "create the correct ordered multi-cert"
    (is 
      (= ["<IfModule mod_proxy.c>"
          "  ProxyPass / http://localhost:8080/"
          "  ProxyPassReverse / http://localhost:8080/"
          "  ProxyPreserveHost On"
          "</IfModule>"]
         (sut/vhost-proxy)         
         ))
    (is 
      (= ["<IfModule mod_proxy.c>"
          "  ProxyPass / http://on.other.host:1234/"
          "  ProxyPassReverse / http://on.other.host:1234/"
          "  ProxyPreserveHost On"
          "</IfModule>"]
         (sut/vhost-proxy
           :target-host "on.other.host"
           :target-port "1234")         
         ))
    )
  )