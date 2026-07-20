# nf-aspera plugin

A Nextflow plugin to use IBM aspera protocol in pipelines

# Maintainers

Please note that this is a **community contributed** plugin and is a collaboration between

1. Jorge Aguilera (@jagedn) as a contributor from Incremental Steps.

## Get Started

To use this plugin in your Nextflow pipeline, add the following to your `nextflow.config` file:

```groovy
plugins {
    id 'nf-aspera'
}
```

## Example

Once configured the plugin you can use the operators provided by the plugin. For example:

```groovy
include { ascp } from 'plugin/nf-aspera'

workflow  {

    Channel.ascp( client: 'demo', destination:'downloads/', source: 'aspera-test-dir-small/10MB.1') | view

}
```


## Building

To build the plugin:
```bash
make assemble
```

## Testing with Nextflow


1. Build and install the plugin to your local Nextflow installation: `make install`
2. Run a pipeline with the plugin: `nextflow run hello -plugins nf-aspera`


## License

This project is licensed under the Apache License 2.0 – see the LICENSE file for details.
