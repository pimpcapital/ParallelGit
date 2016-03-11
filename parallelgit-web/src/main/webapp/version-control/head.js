app.factory('Head', function(Commit) {
  function Head(attributes) {
    this.ref = attributes.ref;
    this.commit = new Commit(attributes.commit);
  }

  Head.prototype.getRef = function() {
    return this.ref;
  };

  Head.prototype.getCommit = function() {
    return this.commit;
  };

  return Head;
});