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
Should command words be keywordized and parameters typed? It would
look a lot cleaner when printing out the dom of an RPP file in the
repl and potentially help processing the data
Problem with this approach is sometimes there are strings in there
which aren't quoted
