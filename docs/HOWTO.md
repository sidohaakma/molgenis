# How to develop and deploy documentation

## Usage

### Development
You can install vuepress globally of locally if you want:

```npm install vuepress -g```

Then you can type ```vuepress dev``` to view the documentation and determine if you have errors in the documentation.

#### Debugging
You get the errors that are in the markdown during the build. You can correct them at dev-time.

### Production
The Jenkinsfile will auto deploy to different versions of the documentation on *.docs.molgenis.org (dependant on the version). There is a helm-chart to serve docs. You need to specify the versions which need to be exposed.

So for instance:

```helm install molgenis-docs```
