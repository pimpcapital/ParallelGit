app.filter('build', function () {
  return function (artifact, type) {
    switch(type) {
      case 'maven':
        return '<groupId>' + artifact.groupId + '</groupId>\n  <artifactId>' + artifact.artifactId + '</artifactId>\n<version>' + artifact.version + '</version>';
      case 'gradle':
        return '\'' + artifact.groupId + ':' + artifact.artifactId + ':' + artifact.version + '\'';
    }
  }
});