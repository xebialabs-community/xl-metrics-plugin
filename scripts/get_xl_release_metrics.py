import base64
import getpass
import json
import sys
import time
import urllib2


def get_metrics(xl_release_url, password):
    metrics_url = xl_release_url + 'api/extension/xl-metrics'
    response = do_request(metrics_url, password)
    if json.loads(response)['status'] != 'started':
        raise BaseException("Could not start calculating metrics:\n%s" % response)

    while True:
        print >> sys.stderr, 'Metrics are being calculated, waiting for completion...'
        time.sleep(10)
        response = do_request(metrics_url, password)
        if json.loads(response)['status'] != 'in_progress':
            return response


def do_request(url, password):
    request = urllib2.Request(url)
    base64string = base64.encodestring('%s:%s' % ('admin', password)).replace('\n', '')
    request.add_header("Authorization", "Basic %s" % base64string)
    return urllib2.urlopen(request).read()


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print "This script gets metrics of your XL Release installation. Please read https://github.com/xebialabs-community/xl-metrics to learn more."
        print "Usage: python get_xl_release_metrics.py http://localhost:5516/my-xlrelease/ [admin password]"
        sys.exit(0)
    xlr_url = sys.argv[1]
    if xlr_url[-1] != '/':
        xlr_url += '/'
    if len(sys.argv) > 2:
        admin_password = sys.argv[2]
    else:
        admin_password = getpass.getpass("Please enter the password of XL Release 'admin' user: ")

    metrics = get_metrics(xlr_url, admin_password)
    print >> sys.stderr, "Finished calculating the metrics:"
    print metrics
