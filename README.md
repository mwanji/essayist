# Essayist

A blogging app for the tent.io protocol. A public instance is available here: http://essayist.mndj.me.

## Quick start:

1. Pre-requisites: Java 1.6+ and [Maven](http://maven.apache.org)
1. Currently, Essayist depends on a Mavenised version of [Migrate4J](https://github.com/mwanji/migrate4j-maven) available only on Github:
`git clone git://github.com/mwanji/migrate4j-maven.git && cd migrate4j-maven && mvn install`
1. `git clone git://github.com/mwanji/essayist.git`
1. Deploy to your favourite servlet container.
1. [http://localhost:8080](http://localhost:8080)
1. Log in with your Tent address, for example https://my_name.tent.is

## Configuration

1. Put a file called essayist.properties in the src/main/resources folder. `essayist-example.properties` shows what can be configured. `essayist-defaults.properties` provides a number of defaults.
1. If using your own database, create an empty database corresponding to the value of db.url

## Developers

### Metadata post type

`http://moandjiezana.com/tent/essayist/types/post/metadata/v0.1.0`

Essayist uses a custom post type to store some data, such as the Essay's text as Markdown. It is linked to the Essay (once it has been published) via the mentions.

<table>
  <thead>
    <tr>
      <th>Property</th><th>Required</th><th>Type</th><th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>format</code></td>
      <td>Required</td>
      <td>String</td>
      <td>Defines the type of content contained in <code>raw</code>. Eg. "markdown".</td>
    </tr>
    <tr>
      <td><code>raw</code></td>
      <td>Required</td>
      <td>String</td>
      <td>The raw form of the metadata. Eg. the markdown form of an Essay.</td>
    </tr>
    <tr>
      <td><code>statusId</code></td>
      <td>Optional</td>
      <td>String</td>
      <td>The ID of a status announcing the Essay.</td>
    </tr>
  </tbody>
</table>

