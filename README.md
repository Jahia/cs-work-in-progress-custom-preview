# cs-work-in-progress-custom-preview
The module is an attempt to create a preview mode that incorporate the "work in progress" status

The custom edit mode configuration will add a new mode called "Advanced Preview (WIP)". This mode is based on the regular preview with an additional HTML filter.
In preview, this filter will display the live version of piece of content flagged as 'work in progress'.

Note that this filter is activated when the parameter "previewmode=wip" is provided to the URL.

In order to use this module:
 - copy the custom edit mode configuration (applicationcontext-customeditmode.xml) into your DX configuration folder (digital-factory-config/jahia/)
 - in order for the module to work, you will need to deactivate the cache filter. In order to do so go to /modules/tools/renderFilters.jsp and deactivate the AggregateCacheFilter
 - deploy the module on your DX instance.
  
  
Please not that this preview has some limitation:
 - for content flagged as "work in progress", renaming its system name will make it disappear from the preview mode
 - moving content flagged as "work in progress" will make it disappear from the preview mode.
 
 
