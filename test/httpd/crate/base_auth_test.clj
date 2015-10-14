; Copyright (c) Michael Jerger. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(ns httpd.crate.base-auth-test
  (:require
    [clojure.test :refer :all]
    [httpd.crate.basic-auth :as sut]
    ))

(deftest vhost-basic-auth-options
  (testing
    (is 
      (= ["AuthName \"Authorization for owncloud.politaktiv.org\"" 
          "AuthType Basic" 
          "AuthBasicProvider file" 
          "AuthUserFile /etc/apache2/htpasswd-owncloud.politaktiv.org" 
          "Require valid-user"] 
         (sut/vhost-basic-auth-options 
           :domain-name "owncloud.politaktiv.org")         
         ))
    )
  
  (testing
    (is 
      (= ["AuthName \"Authorization for owncloud.politaktiv.org\"" 
          "AuthType Basic" 
          "AuthBasicProvider file" 
          "AuthUserFile /etc/apache2/htpasswd-owncloud.politaktiv.org" 
          "Require env auth_not_required"
          "Require valid-user"] 
         (sut/vhost-basic-auth-options 
           :domain-name "owncloud.politaktiv.org"
           :authz-options
           ["Require env auth_not_required"
            "Require valid-user"])         
         ))
    )
  )
