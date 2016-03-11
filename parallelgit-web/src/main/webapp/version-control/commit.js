app.factory('Commit', function(Person) {
  function Commit(attributes) {
    this.hash = attributes.hash;
    this.message = attributes.message;
    this.author = new Person(attributes.author);
    this.committer = new Person(attributes.committer);
  }
  return Commit;
});