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
V need to use the pipeline of the selected step! not just reuse the pipeline that we already had
	> this should also solve the problem that apparently "set values" are not removed
V allow dragging of services so you can position them as you want
V start exposing service attributes
	> check if "for" loops etc indeed update the pipeline visually! (with correct type!)
V allow adding of services by dragging them from the repository tree
V allow setting of fixed values! no need to support list entries for strings > when you select manually set value, you can set index
	> can still link to other indexes
	> if need multiple hardcoded values, use services (easier with service chaining)
	> lists of structures? maybe next release?
V resize pipeline left & right to fit the width of the tree + some space for lines
V allow setting of indexes for a link (use properties screen)
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