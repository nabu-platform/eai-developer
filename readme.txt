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
- allow dragging of services so you can position them as you want
- allow adding of services by dragging them from the repository tree
- make sure "invocation order" is calculated correctly (provide a method in map to recalculate)
	> exceptions if there are circular maps! so basically calculated before actually adding the mapping!
- start exposing service attributes
	> check if "for" loops etc indeed update the pipeline visually! (with correct type!)
V resize pipeline left & right to fit the width of the tree + some space for lines

Known errors
------------
If you extend a type (or no type), add a field of say name "test"
You then add a supertype and that supertype already has that name, this is currently allowed
However when you reload the type, it will give you an error:
Exception in thread "JavaFX Application Thread" java.lang.IllegalStateException: A child type restricts a parent type with an invalid restriction