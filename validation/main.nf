include { withAspera } from 'plugin/nf-aspera'


    Channel.withAspera([
        client: 'asperasoft',
        destination:'downloads/',
        sources:[
            'aspera-test-dir-large/100MB',
	    ]
    ])
    | view
