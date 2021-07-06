#!/bin/bash

git config --global user.name nillerusr
git config --global user.email nillerusr@users.noreply.github.com

generate_readme() # maybe later...
{
echo
}

upload()
{
	# Create new repo with new files
	mkdir deploy
	cp $* deploy/
	cd deploy
	git init
	git remote add deploy https://nillerusr:${GH_TOKEN}@github.com/nillerusr/srceng-deploy.git
	git checkout -b $DEPLOY_BRANCH
	generate_readme
	git add .
	git commit -m "Latest workflow deploy"
	git push -q --force deploy $DEPLOY_BRANCH &> /dev/null
}

upload $*
