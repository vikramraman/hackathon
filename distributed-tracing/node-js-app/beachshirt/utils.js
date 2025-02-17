const log4js = require('log4js')
const request = require('request');
const { Tags, FORMAT_HTTP_HEADERS } = require('opentracing');

const _envTags = ['staging', 'production', 'development'];
const _locationTags = ['palo-alto', 'san-francisco', 'new-york'];
const _tenantTags = ['wavefront', 'vmware'];

// prepares the base url
exports.getBaseUrl = (service) => {
    return `http://${service.host}:${service.port}`;
}

// prepares the logger
exports.getLogger = () => {
  // Configure the logger
  log4js.configure({
    appenders: {
      everything: { type: 'file', filename: 'beachshirt.log' }
    },
    categories: {
      default: { appenders: [ 'everything' ], level: 'debug' }
    }
  });
  return log4js.getLogger();
}

// Start the server
exports.startServer = (app, service, log) => {
  app.listen(service.port, () => {
    log.debug(`${service.service} service is listening on port ${service.port}`)
  }).on('error', (error) => {
    log.error(`Unable to start ${service.service} service on port ${service.port}. Error: ${error.message}`);
  });
}

// call get request
exports.getRequest = (res, api, param, service, span, tracer) => {
  let http_url = `${this.getBaseUrl(service)}${api}`;
  let http_headers = {"Content-Type":"application/json"};
  span.setTag(Tags.HTTP_URL, http_url);
  span.setTag(Tags.HTTP_METHOD, 'GET');
  span.setTag(Tags.SPAN_KIND, Tags.SPAN_KIND_RPC_CLIENT);
  // Send span context via request headers (parent id etc.)
  tracer.inject(span, FORMAT_HTTP_HEADERS, http_headers);

  request.get({
    url: http_url,
    headers: http_headers,
    qs: param},
    (error, response, body) => {
        if(error){
            span.setTag(Tags.HTTP_STATUS_CODE, 500)
            span.setTag(Tags.ERROR, true)
            span.finish();
            return res.status(500).json({'error': error.message})
        }
        span.setTag(Tags.HTTP_STATUS_CODE, response.statusCode)
        span.finish();
        return res.status(response.statusCode).json(JSON.parse(body));
     });
} 

// call post request
exports.postRequest = (res, api, formData, service, span, tracer) => {
  const httpUrl = `${this.getBaseUrl(service)}${api}`;
  const httpHeaders = {"Content-Type":"application/json"};
  span.setTag(Tags.HTTP_URL, httpUrl);
  span.setTag(Tags.HTTP_METHOD, 'POST');
  span.setTag(Tags.SPAN_KIND, Tags.SPAN_KIND_RPC_CLIENT);
  // Send span context via request headers (parent id etc.)
  tracer.inject(span, FORMAT_HTTP_HEADERS, httpHeaders);

  request.post({
    url: httpUrl,
    headers: httpHeaders,
    form: formData
  }, (error, response, body) => {
    if(error){
        span.setTag(Tags.HTTP_STATUS_CODE, 500);
        span.setTag(Tags.ERROR, true);
        span.finish();
        return res.status(500).json({error: error.message});
    }
    span.setTag(Tags.HTTP_STATUS_CODE, response.statusCode);
    span.finish();
    return res.status(response.statusCode).json(JSON.parse(body));
 })
}

// returns a set of custom span tags
exports.getCustomTags = () => {
    return {
        'env': _envTags[this.getRandomInt(_envTags.length)],
        'location': _locationTags[this.getRandomInt(_locationTags.length)],
        'tenant': _tenantTags[this.getRandomInt(_tenantTags.length)]
    };
}

// returns a random integer in the range [0, max)
exports.getRandomInt = (max) => {
    return Math.floor(Math.random() * Math.floor(max));
}