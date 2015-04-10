# This test is not executed until the environment to execute tests in plugins is defined
describe 'XL Metrics', ->
  globalForEach()

  getMetrics = ->
    Requests.get('api/extension/xl-metrics').then (metricsResult) ->
      if (metricsResult.getBody().status == "completed")
        metricsResult
      else
        Q.delay(100).then getMetrics

  it 'should return cis count', ->
    getMetrics().then (metrics) ->
      @totalCisCount = metrics.getBody().totalCisCount
      fixtures().release(id: 'ReleaseMetrics', status: 'in_progress')
    .then getMetrics
    .then (metrics) ->
      expect(metrics.getBody().totalCisCount).to.be.above @totalCisCount

  it 'should return per ci-type count', ->
    getMetrics().then (metrics) ->
      @releasesCount = metrics.getBody().cisCounts['xlrelease.Release']
      fixtures().release(id: 'ReleaseMetrics', status: 'in_progress')
    .then getMetrics
    .then (metrics) ->
      expect(metrics.getBody().cisCounts['xlrelease.Release']).to.be.above @releasesCount

  it 'should return per ci-type revision\'s count', ->
    getMetrics().then (metrics) ->
      @releasesRevisionsCount = metrics.getBody().revisionsCounts['xlrelease.Release']
      fixtures().release(id: 'ReleaseMetrics', status: 'in_progress', dueDate: moment().toDate(), scheduledStartDate: moment().toDate())
    .then ->
      Requests.get('api/v1/releases/Applications/ReleaseMetrics')
    .then (response) ->
      release = response.getBody()
      release.title = 'Updated title'
      Requests.put('api/v1/releases/Applications/ReleaseMetrics', release)
    .then getMetrics
    .then (metrics) ->
      expect(metrics.getBody().revisionsCounts['xlrelease.Release']).to.be.above @releasesRevisionsCount
