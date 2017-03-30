# cs-work-in-progress-custom-preview
The module is an attempt to create a preview mode that does not show the content in "work in progress" status in the preview mode

The custom edit mode configuration will add a new mode called "Advanced Preview (WIP)". This mode is based on the regular preview with an additional HTML filter.
In preview, this filter will display the live version of piece of content flagged as 'work in progress'.

Note that this filter is activated when the parameter "previewmode=wip" is provided to the URL.

In order to use this module:
 - stop your Jahia instance
 - copy the custom edit mode configuration (applicationcontext-customeditmode.xml) into your DX configuration folder (digital-factory-config/jahia/)
 - start your Jahia instance
 - deploy the module on your DX instance.
  
 
 
