<#include "header.ftl">
  <div class="container bump-me">

    <div class="body-content">

      <div class="panel panel-default">
        <div class="panel-heading">
          <h3 class="panel-title">Past Recordings</h3>
        </div>
        <div class="panel-body">
          <#if (archives?size > 0) >
          <table class="table">
            <thead>
              <tr>
                <th>&nbsp;</th>
                <th>Created</th>
                <th>Duration</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <#list archives as item>

              <tr data-item-id="${ item.id }">
                <td>
                  <#if (item.status = "available" && item.url?? && item.url?length > 0)>
                  <a href="/download/${ item.id }">
                  </#if>
                  ${ item.name!"Untitled" }
                  <#if (item.status = "available" && item.url?? && item.url?length > 0)>
                  </a>
                  </#if>
                </td>
                <td>${ item.createdAt?number_to_datetime }</td>
                <td>${ item.duration } seconds</td>
                <td>${ item.status }</td>
                <td>
                  <#if (item.status == 'available')>
                    <a href="/delete/${ item.id }">Delete</a>
                  <#else>
                    &nbsp;
                  </#if>
                </td>
              </tr>

              </#list>
            </tbody>
          </table>
          <#else>
          <p>
            There are no archives currently. Try making one in the <a href="/host">host view</a>.
          </p>
          </#if>
        </div>
        <div class="panel-footer">
          <#if showPrevious??>
            <a href="${ showPrevious }" class="pull-left">&larr; Newer</a>
          </#if>
          &nbsp;
          <#if showNext??>
            <a href="${ showNext }" class="pull-right">Older &rarr;</a>
          </#if>
        </div>
      </div>
    </div>
  </div>
<#include "footer.ftl">