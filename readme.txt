developer
---------
update left tree to allow rename + drag-to-move + delete
in the "map" view

java
-----
have annotations for parameter names (currently just arg0 etc)
have custom annotations except for webservice & xmlrootelement to do it "cleanly"


permissions
-----------
have permissions in node.xml (+ default for all)
in repository the getnode will require a principal for all requests
per node, we should be able to edit the permissions in the developer
for each endpoint, should be able to clearly set permissions?

components:
- http(s) endpoint
- key store
- proxy server
- rest service (uses http endpoint) > should be able to set multiple endpoints? mostly http & https
- way to add resources (javascript, css, pages,... to http)
- soap webservice


Structure modeling
------------------
- repository browser: update tree item to allow for soft refresh (see element & step)
- need new custom tree view for pipeline management, can't reuse structure
- drag/drop from repository into structure & map step (services & structures)
- when removing all custom children & there is a supertype > reference instead of extension!
- more type buttons
- an "optional" indication
- more menu buttons

ToDo
----
- make sure "invocation order" is calculated correctly (provide a method in map to recalculate)
	> exceptions if there are circular maps! so basically calculated before actually adding the mapping!
	> perform invocation order calculation on each load of a map step
	> in an overview of all the problems with the service
- still some binding bugs (lines shift etc), not sure if this is javafx or me
- need to prettify
- need to make sure in a "for" loop that the variable is _not_ editable
- need to allow you to add a "sequence" to a switch (in the background, actually use a case)
- new buttons for catch, finally,...
- need to make sure that invocation variables (temporarily mapped) are _not_ visible!

Known errors
------------
If you extend a type (or no type), add a field of say name "test"
You then add a supertype and that supertype already has that name, this is currently allowed
However when you reload the type, it will give you an error:
Exception in thread "JavaFX Application Thread" java.lang.IllegalStateException: A child type restricts a parent type with an invalid restriction