app.filter('build', function () {
  return function (artifact, type) {
    switch(type) {
      case 'maven':
        return '<dependency>\n  <groupId>' + artifact.groupId + '</groupId>\n    <artifactId>' + artifact.artifactId + '</artifactId>\n  <version>' + artifact.version + '</version>\n</dependency>';
      case 'gradle':
        return '\'' + artifact.groupId + ':' + artifact.artifactId + ':' + artifact.version + '\'';
    }
  }
});