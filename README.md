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
 * production grade hardening
 * maintainance page in case of appserver frontend
 * googles web-id
 * mod-jk configuration
 * basic auth
 * taller monitoring configs
 * config of max-clients (including maxfiles) 

## Usage

Take a look at the code inside src/httpd/groups/httpd.clj for example
of how I use this crate to install apache and set up a reverse proxy
to a backend java servlet.

## TODO's
* config phase @vhost: 
  * install (gnutls) certs
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
  * hardening
   * ports, limit, logging, security
  * gnutls
  	* mods/gnutls
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
