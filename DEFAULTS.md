# Default behaviors in Desku

### Tipp: Do not use 100% width
Use flex-grow css attribute instead, since its better to manage.


### Width stretching
Almost all components stretch/grow their width by default except some like Text, Image
and Spinner for obvious reasons.
We do this to simplify creating responsive layouts and reduce the amount of code needed.

If you do not want this to be the default for your component you can simply give it another
HTML tag than "c". For most people simply setting something like `comp.maxWidth("10%")` should be enough though.

### Height overflow
We leave the height of components unset so that they can overflow/expand their container.
A scroll bar will be shown in that case by default. This again is done to reduce the amount of code written.