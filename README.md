# rpp-clj
Parser for Reaper project files

# Rationale
Reaper is my favourite DAW. Sometimes the user interface trips me and
I end up making edits that I regret. A lot of them. In these cases
sometimes I've written tools for automatically correct the mistakes I
made. Sometimes it's easy, sometimes it's not. The RPP file format
used by Reaper is not too easy to parse and process, as it is "almost
XML" but not quite.

Therefore, I decided to write a parser which I can use to help create my
Reaper project cleaning tools.


# TODO
Is it a good idea to furhter parse the arguments? Currently everything
is considered a String argument. It might be easier to manipulate the
DOM if those types would be changed to native types. The problem with
this approach is that all of the String typed arguments aren't
systemically quoted. That makes it somewhat harder to render the DOM
as a Reaper project file when your edits have been done. I didn't
test if Reaper would accept quoted strings everywhere yet.
