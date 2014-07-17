Structure modeling
------------------
- repository browser: update tree item to allow for soft refresh (see element & step)
- need new custom tree view for pipeline management, can't reuse structure
- drag/drop from repository into structure & map step (services & structures)
- when removing all custom children & there is a supertype > reference instead of extension!
- more type buttons
- an "optional" indication
- more menu buttons

Known errors
------------
If you extend a type (or no type), add a field of say name "test"
You then add a supertype and that supertype already has that name, this is currently allowed
However when you reload the type, it will give you an error:
Exception in thread "JavaFX Application Thread" java.lang.IllegalStateException: A child type restricts a parent type with an invalid restriction