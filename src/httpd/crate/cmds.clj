; Copyright (c) Michael Jerger, Dave Paroulek. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.
;
;; fn's to call apache2 commands on server

(ns httpd.crate.cmds
  (require 
    [pallet.actions]))

(defn apache2ctl
  "Start, stop, restart with apache2ctl command"
  [cmd]
  (pallet.actions/exec nil (str "sudo apache2ctl " cmd)))

(defn a2enmod
  "Enable apache modules such as mod_rewrite, mode_proxy, etc"
  [mod]
  (pallet.actions/exec nil (str "a2enmod " mod)))

(defn a2ensite
  "Enable vhost site. vhost-conf should be the name of a conf file in
  sites-available"
  [vhost-conf]
  (pallet.actions/exec nil (str "a2ensite " vhost-conf)))

(defn a2dissite
  "Disable vhost site. vhost-conf should be the name of a conf file in
  sites-available"
  [vhost-conf]
  (pallet.actions/exec nil (str "a2dissite " vhost-conf)))

(defn a2enconf
  "Enable apache2 conf file. vhost-conf should be the name of a conf file in
  conf-available"
  [vhost-conf]
  (pallet.actions/exec nil (str "a2enconf " vhost-conf)))
