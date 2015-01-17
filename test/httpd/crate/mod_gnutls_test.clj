; Copyright (c) Michael Jerger. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(ns httpd.crate.mod-gnutls-test
  (:require
    [clojure.test :refer :all]
    [pallet.actions :as actions]
    [httpd.crate.mod-gnutls :as sut]
    ))

(deftest create-certs
  (testing
    "create the correct ordered multi-cert"
    (is 
      (= ["domain-cert"
          "intermediate-cert"
          "ca-cert"
          ""]
         (sut/gnutls-certs 
           :domain-name "owncloud.politaktiv.org" 
           :domain-cert "domain-cert" 
           :intermediate-certs ["intermediate-cert"]
           :ca-cert "ca-cert")         
         ))
    )
  )