#### CayenneScala

Creating the object

``` scala      
Article("Content") commit
```
or just

``` scala
Article("Content 2") !
```
      
Finding entities

``` scala      
var seq1 = Article() find
```

or just

``` scala
var seq2 = Article() ~
```