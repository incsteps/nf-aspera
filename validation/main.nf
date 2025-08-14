include {fromAspera} from 'plugin/nf-aspera'


    Channel.fromAspera([
        client: 'asperasoft',
        destination:'downloads/',
        sources:[
            'aspera-test-dir-large/100MB',
	]
    ])
    | view
