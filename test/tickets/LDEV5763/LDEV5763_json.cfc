component output=false
    javasettings = '{
		maven = ["org.eclipse.jgit:org.eclipse.jgit:7.3.0.202506031305-r"]
	}'
{
	public function test(){

	}

	function readFileCommits(required string repoPath, required string filePath) {
		var commits = [];
		try {
			var FileRepository = createObject(
				"java",
				"org.eclipse.jgit.storage.file.FileRepositoryBuilder"
			);
			var repository = FileRepository().setGitDir(createObject(
				"java", "java.io.File", repoPath & "/.git"
			)).readEnvironment().findGitDir().build();

			var Git = createObject("java", "org.eclipse.jgit.api.Git");
			var git = Git.init().setDirectory(createObject("java", "java.io.File", repoPath)).call();

			// Use LogCommand to get list of commits for the file
			var logCmd = git.log().addPath(arguments.filePath);
			var commitIter = logCmd.call().iterator();

			while (commitIter.hasNext()) {
				var commit = commitIter.next();
				commits.append(commit.getName()); // getName() returns the commit hash
			}

			repository.close();
		} catch (any e) {
			throw(message="Failed to enumerate commits: " & e.message, cause=e);
		}
		return commits;
	}

}
