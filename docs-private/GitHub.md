# GitHub

This project `enrollment-server-wultra` is a downstream project of `enrollment-server`.


## Setup a remote repository

```shell
git remote add upstream git@github.com:wultra/enrollment-server.git
```

See [Configuring a remote repository for a fork](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/working-with-forks/configuring-a-remote-repository-for-a-fork) for details.


## Merge upstream

```shell
git checkout -b issues/merge-upstream
git fetch upstream
git merge upstream/develop
git push -u origin issues/merge-upstream
```
