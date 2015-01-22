# Pallet create for Apache httpd

This is a crate to install and run httpd via Pallet.

Currently it's pretty basic, just the bare minimum for what I need at
the moment - it can install Apache2 on Ubuntu and then do some small
configuration tasks like enable mods, and set up a virtual host.

I'll update to work with more servers and config options as needed but
that probably won't be for a while. 

## Features
 * creation of complex vhost files
 * secure https (gnutls) config - proven at https://www.ssllabs.com/ssltest/
 * production grade apache hardening
 
## Usage Examples

(defn install-webserver
  []
  (apache2/install-apache2)
  (gnutls/install-mod-gnutls)
  )

(defn configure-webserver
  [& {:keys [domain-name 
             domain-cert 
             domain-key 
             ca-cert]}]
  (apache2/config-apache2-production-grade
    :security 
    (apache2/security))
  (gnutls/configure-gnutls-credentials
    :domain-name domain-name
    :domain-cert domain-cert
    :domain-key domain-key
    :ca-cert ca-cert)
  (apache2/config-and-enable-vhost
    "default-000"
    (vhost/vhost-conf-default-redirect-to-https-only
      :domain-name domain-name  
      :server-admin-email (str "admin@" domain-name)))
  (apache2/config-and-enable-vhost
    "default-ssl-000"
    (some-own-vhost-definition
      :domain-name domain-name  
      :server-admin-email (str "admin@" domain-name)))
  )

## TODO's

 * config of max-clients
 * maintainance page in case of appserver frontend
 * googles web-id
 * mod-jk configuration
 * basic auth
 * taller monitoring configs
  

### some config snippets to be realized on demand
* config phase @vhost: 
  * support for base auth
  
   <Location />
    Order deny,allow
    Deny from <%= deny_from %>
    AuthType Basic     
    AuthName "<%= fqdn %>" 
    
    Satisfy Any
    AuthUserFile <%= base_auth_target %>
    Require valid-user
  </Location>
  
  * support for google-website-id
  
   Alias /<%= google_token %>.html "/var/www/static/google/<%= google_token %>.html"

 * conf phase @ server:
  * mod-jk
   * mods/jk
   * etc/libapache2-mod-jk/workers.properties
  * maintainance-page
   * error
   * /var/www/static
  * monitoring / load-testing
   * sysstat
  * config of max-clients (including maxfiles)
  

## License

Copyright © 2015, Dave Paroulek

Distributed under the Eclipse Public License.