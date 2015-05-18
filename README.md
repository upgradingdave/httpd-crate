# Pallet Crate for Apache httpd

This is a crate to install, configure and run httpd via Pallet.

Currently this crate can install Apache2 on Ubuntu and then do several
configuration tasks (described below).

If you are interested in enhancing this to provide additional
configuration options or to work with other linux flavors,
contributions are welcome!

## compatability

This crate is working with:
 * pallet 0.8
 * ubuntu 14.04
 * apache httpd 2.4

## Features
 * creation of complex vhost files
 * use https (gnutls). Config is proven at https://www.ssllabs.com/ssltest/
 * use proxy_http
 * use basic authentication 
 * production grade apache hardening
 
## Usage Examples

Required dependencies

    (require '[pallet.actions :as actions])
    (require '[pallet.api :refer [group-spec server-spec node-spec plan-fn]])
    (require '[pallet.crate.automated-admin-user :refer [automated-admin-user]])
    (require '[httpd.crate.apache2 :as apache2])
    (require '[httpd.crate.cmds :as cmds])

    (use 'pallet.repl)

Configure a base server that allows us to connect via ssh using
private key and also automatically runs apt-get update

    (def base-server
      (server-spec
        :phases
        {:bootstrap (plan-fn 
            ;; setup private key ssh
            (automated-admin-user)
            ;; update packages
            (actions/package-manager :update))}))

Create the pallet service and node-spec

    (def s (pallet.configure/compute-service :vmfest))
    (def default-node-spec
      (node-spec
        :image {:image-id :ubuntu-14.04}
        :hardware {:min-cores 1}))
        
Define a group-spec that extends `apache2/server-spec`. The
`apache2/server-spec` will install apache2 package during configure
phase and adds a `:restart` :phase for conveniently restarting apache.
If/when someone has time, we want to eventually provide a lot more
options so that the `apache2/server-spec` can control much more.

    (def apache2
      (group-spec "apache2"
        :extends [base-server 
             (apache2/server-spec {})]
        :node-spec default-node-spec))

Use converge to bring up http server and install apache2

    (session-summary
      (pallet.api/converge {apache2 1} :compute s))

Enable a couple mods and restart

    (session-summary
      (pallet.api/converge {apache2 1}
          :compute s
          :phase (plan-fn (cmds/a2enmod "rewrite")
                          (cmds/a2enmod "headers")
                          (cmds/apache2ctl "restart"))))

Setup mod-gnutls

    (require '[httpd.crate.mod-gnutls :as gnutls])
    (session-summary
      (pallet.api/converge {apache2 1}
          :compute s
          :phase (plan-fn (gnutls/install-mod-gnutls)
                          (gnutls/configure-gnutls-credentials
                              :domain-name "your-domain.com"
                              :domain-cert "your cert file"
                              :domain-key "your key file"
                              :ca-cert "your cert file"))))

Setup mod-proxy-http

    (require '[httpd.crate.mod-proxy-http :as proxy])
    (session-summary
      (pallet.api/converge {apache2 1}
          :compute s
          :phase (plan-fn (proxy/install-mod-proxy-http))))

Configure limits

    (require '[httpd.crate.config :as conf])
    (session-summary
      (pallet.api/converge {apache2 1}
          :compute s
          :phase (plan-fn (apache2/configure-file-and-enable
                           "limits.conf" conf/limits))))

Configure security

    (session-summary
        (pallet.api/converge {apache2 1}
            :compute s
            :phase (plan-fn (apache2/configure-file-and-enable
                             "security.conf" conf/security))))

Configure ports

    (session-summary
        (pallet.api/converge {apache2 1}
            :compute s
            :phase (plan-fn (apache2/configure-file-and-enable
                             "ports.conf" conf/ports))))
                             
Setup mod_jk

    (require '[httpd.crate.mod-jk :as jk])
    (session-summary
      (pallet.api/converge {apache2 1}
          :compute s
          :phase (plan-fn (gnutls/install-mod-jk)
                          (gnutls/configure-jk-worker))))


Setup vhosts. Here's an example of a fairly complicted vhost: 

    (require '[httpd.crate.vhost :as vhost])
    (require '[httpd.crate.basic-auth :as auth])

    (def vhost-content
      (into 
      []
        (concat
          (vhost/vhost-head :listening-port "443"
                            :domain-name "domain-name" 
                            :server-admin-email "server-admin-email")
          (proxy/vhost-proxy :target-port "app-port") 
          (vhost/vhost-location
             :location-options
             (auth/vhost-basic-auth-options :domain-name "domain-name"))
          (vhost/vhost-log 
           :error-name "error.log"
           :log-name "ssl-access.log"
           :log-format "combined")
          (gnutls/vhost-gnutls "domain-name")
          vhost/vhost-tail)))

    (session-summary
        (pallet.api/converge {apache2 1}
            :compute s
            :phase (plan-fn (apache2/configure-and-enable-vhost
                             "000-default" vhost-content))))

There are a few other fn's inside `apache2.crate.vhost` such as
`vhost/vhost-conf-default-redirect-to-https-only` that are convenient
for creating content to pass to `apache2/configure-and-enable-vhost`

## TODO's

 * config of max-clients
 * maintainance page in case of appserver frontend
 * googles web-id
 * taller monitoring configs

### some config snippets to be realized on demand
* config phase @vhost:
  
  * support for google-website-id
  
   Alias /<%= google_token %>.html "/var/www/static/google/<%= google_token %>.html"

 * conf phase @ server:
  * maintainance-page
   * error
   * /var/www/static
  * config of max-clients (including maxfiles)


## License

Copyright © 2015, Dave Paroulek

Distributed under the Eclipse Public License.
