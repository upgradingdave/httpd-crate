# Pallet create for Apache httpd

This is a crate to install and run httpd via Pallet.

Currently it's pretty basic, just the bare minimum for what I need at
the moment - it can install Apache2 on Ubuntu and then do some small
configuration tasks like enable mods, and set up a virtual host.

I'll update to work with more servers and config options as needed but
that probably won't be for a while. 

## Usage

Take a look at the code inside src/httpd/groups/httpd.clj for example
of how I use this crate to install apache and set up a reverse proxy
to a backend java servlet. 

## License

Copyright © 2015, Dave Paroulek

Distributed under the Eclipse Public License.
