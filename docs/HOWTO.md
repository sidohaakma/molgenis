# How to develop and deploy documentation

## Usage

### Development
You type ```vuepress dev``` to vue the documentation and determine if you have errors in the documentation.

### Production
The Jenkinsfile will auto deploy to different versions of the documentation on *.docs.molgenis.org (dependant on the version). There is a helm-chart to serve docs. You need to specify the versions which need to be exposed.

So for instance:

```helm install molgenis-docs --deployedVersions="8.0,9.1"```
