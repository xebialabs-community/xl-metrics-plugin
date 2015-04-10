# XL Metrics plugin

This plugin creates a new endpoint in your XL Release or XL Deploy server which allows you to gather several metrics about your XL* instance. In particular it gives you some insight into the size of the repository, by counting numbers of configuration items per type.

## Requirements

* XL Release 4.6.0 or higher.
* XL Deploy 5.0.0 (including betas) or higher.

## How to install it

* Download the [`xl-metrics-plugin-1.0.jar`](https://github.com/xebialabs-community/xl-metrics-plugin/releases/download/v1.0/xl-metrics-plugin-1.0.jar) file.
* Copy it into the **plugins/** directory of the XL Release or XL Deploy instance.
* Restart the instance.

## How to get metrics

### Easy way

For XL Release we have an utility script which gets the metrics for you.

1. Download the Python script [`get_xl_release_metrics.py`](src/scripts/get_xl_release_metrics.py).
2. Run it like this:


    python get_xl_release_metrics.py http://localhost:5516/my-xlrelease/
    
It will ask you for admin password, start generating the metrics, wait until they are ready and print the result into standard output.

### Full way

This plugin adds a new endpoint: 

* `GET /api/extension/xl-metrics` for XL Release.
* `GET /api/xl-metrics` for XL Deploy.

This endpoint is stateful, so you can use the same request to first trigger the calculation of metrics, then to get the status and get the result.

For example following `curl` command will trigger the metrics calculation in XL Release, assuming that XL Release is running on http://localhost:5516/my-xlrelease:

    curl -u admin:password http://localhost:5516/my-xlrelease/api/extension/xl-metrics
    
On XL Deploy the URL is slightly different:

    curl -u admin:password http://localhost:4516/my-xldeploy/api/xl-metrics

To get the result you need to make the same request after a while. If the calculation has finished, then you will get a JSON response with the result:

    {
      "totalCisCount": 195,
      "cisCounts": {
        "xlrelease.GateCondition": 24,
        "xlrelease.Team": 21,
        "core.Directory": 9,
        "xlrelease.DeployitTask": 3,
        "xlrelease.ParallelGroup": 7,
        "xlrelease.ActivityLogEntry": 17,
        "xlrelease.NotificationTask": 6,
        "xlrelease.UserProfile": 1,
        "xlrelease.GateTask": 17,
        "internal.Root": 4,
        "xlrelease.Phase": 17,
        "xlrelease.Release": 7,
        "xlrelease.Task": 62
      },
      "revisionsCounts": {
        "xlrelease.GateCondition": 0,
        "xlrelease.Team": 0,
        "core.Directory": 0,
        "xlrelease.DeployitTask": 0,
        "xlrelease.ParallelGroup": 0,
        "xlrelease.ActivityLogEntry": 0,
        "xlrelease.NotificationTask": 0,
        "xlrelease.UserProfile": 0,
        "xlrelease.GateTask": 0,
        "internal.Root": 0,
        "xlrelease.Phase": 2,
        "xlrelease.Release": 2,
        "xlrelease.Task": 2
      },
      "customQueriesCounts": {
        "numberOfCompletedReleases": 0,
        "numberOfActiveReleases": 2,
        "numberOfTemplates": 5,
        "numberOfPlannedReleases": 0,
        "numberOfEnabledTriggers": 0,
        "numberOfRunningScriptTasks": 0
      },
      "completed": true
    }

Otherwise you will get a message that calculation is still in progress.

**Notes**:

* Only the _admin_ user can work with metrics.
* Calculation of metrics can take a lot of time depending on your repository size. For example on a repository of 1500 releases in XL Release it took approximately 4 minutes to finish.

## How to create your own metrics

You can add custom queries to count number of specific objects in your repository. To do that you can create an `XL_HOME/conf/xl-metrics.conf` file, where `XL_HOME` is the directory where your XL Release or XL Deploy is installed. This file must be defined like this:

    xl-release {
      queries {
        yourFirstQuery = "SELECT ci.[$configuration.item.type] FROM [deployit:configurationItem] AS ci WHERE ((ci.[$configuration.item.type] = 'xlrelease.Release') AND (ci.status = 'COMPLETED' OR ci.status = 'ABORTED'))"
        yourSecondQuery = "SELECT ci.[$configuration.item.type] FROM [deployit:configurationItem] AS ci WHERE ((ci.[$configuration.item.type] = 'xlrelease.Release') AND (ci.status = 'IN_PROGRESS' OR ci.status = 'FAILED' OR ci.status = 'FAILING' OR ci.status = 'PAUSED'))"
        ...
      }
    }
    xl-deploy {
      queries {
        yourXlDeployQuery = "..."
      }
    }

So custom queries are defined per product. You don't have to specify both `xl-release` and `xl-deploy` blocks, the above snippet is just an example.

The language used for writing queries is [JCR QL](http://www.day.com/specs/jcr/2.0/6_Query.html) with an additional helper expression which lets you  check if a CI has a subtype of a given type:

    isSubTypeOf[alias:type]

Where:

* alias is the query alias,
* type is the synthetic parent type.

After you restart XL* and get the metrics again, a number of objects found by your query will be printed in the result, for example:

    {
      ...
      "customQueriesCounts": {
        "yourFirstQuery": 0,
        "numberOfCompletedReleases": 0,
        ...
      },
      ...
    }

Note that in your custom `xl-metrics.conf` file you add _new_ queries to metrics, [bundled queries](src/main/resources/xl-metrics.conf) like `numberOfCompletedReleases` will still be executed. If you want to exclude a bundled query you can do it like this:

    xl-release {
      queries {
        numberOfCompletedReleases = null
      }
    }


### Some example queries :

    xl {
      queries {
        numberOfCompletedReleases = "SELECT ci.[$configuration.item.type] FROM [deployit:configurationItem] AS ci WHERE ((ci.[$configuration.item.type] = 'xlrelease.Release') AND (ci.status = 'COMPLETED' OR ci.status = 'ABORTED'))"
        numberOfActiveReleases = "SELECT ci.[$configuration.item.type] FROM [deployit:configurationItem] AS ci WHERE ((ci.[$configuration.item.type] = 'xlrelease.Release') AND (ci.status = 'IN_PROGRESS' OR ci.status = 'FAILED' OR ci.status = 'FAILING' OR ci.status = 'PAUSED'))"
        numberOfTemplates = "SELECT ci.[$configuration.item.type] FROM [deployit:configurationItem] AS ci WHERE ((ci.[$configuration.item.type] = 'xlrelease.Release') AND (ci.status = 'TEMPLATE'))"
        numberOfPlannedReleases = "SELECT ci.[$configuration.item.type] FROM [deployit:configurationItem] AS ci WHERE ((ci.[$configuration.item.type] = 'xlrelease.Release') AND (ci.status = 'PLANNED'))"
        numberOfRunningScriptTasks = "SELECT ci.[$configuration.item.type] FROM [deployit:configurationItem] AS ci WHERE ((ci.[$configuration.item.type] = 'xlrelease.ScriptTask' OR ci.[$configuration.item.type] = 'xlrelease.CustomScriptTask') AND (ci.status = 'IN_PROGRESS'))"
        numberOfEnabledTriggers = "SELECT ci.[$configuration.item.type] FROM [deployit:configurationItem] AS ci WHERE (isSubTypeOf[ci:xlrelease.ReleaseTrigger] AND (ci.enabled = 'true'))"
      }
    }
