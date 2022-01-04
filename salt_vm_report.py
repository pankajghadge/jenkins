import os
import json
import sys

instances = ['VM_NAME_1', 'VM_NAME_2', 'VM_NAME_3', 'VM_NAME_4', 'VM_NAME_5']

def generate_html_table():

    table_output = ""
    for salt_host in instances:
        with open('/tmp/salt/'+salt_host+'.json') as f:
            salt_grain_items = json.load(f)

        with open('/tmp/salt/'+salt_host+'-disk.json') as f:
            salt_disk_items = json.load(f)

        table_output += '<table id="projectstatus" class="sortable pane bigtable stripped-odd" style="width:100%">'
        table_output += '<thead><tr><th colspan="9" style="text-align: center; vertical-align: middle;">'+ salt_host +'</th></tr></thead>'
        table_output += '<thead>'
        table_output += '<tr>'
        table_output += '<th>Host</th>'
        table_output += '<th>IP</th>'
        table_output += '<th>CPU</th>'
        table_output += '<th>Memory</th>'
        table_output += '<th>OS</th>'
        table_output += '<th>OS Release</th>'
        table_output += '<th>Root size</th>'
        table_output += '</tr>'
        table_output += '</thead>'
        table_output += '<tbody>'

        for host, grain in salt_grain_items.items():
            ip  = grain["fqdn_ip4"][0]
            cpu = grain["num_cpus"]
            mem = grain["mem_total"]
            mem = mem/1024
            os  = grain["os"]
            if '/' in salt_disk_items[host]:
               disk = round(salt_disk_items[host]['/']['total']/(1024 * 1024 * 1024),2)
            elif 'total' in salt_disk_items[host]:
               disk = salt_disk_items[host]['total']
            else:
               disk = "NA"
            osrelease = grain["osrelease"]
            table_output += '<tr>'
            table_output += '<td>'+host+'</td><td>'+ip+'</td><td>'+str(cpu)+'</td><td>'+str(round(mem,2))+' GB</td><td>'+geo+'</td><td>'+os+'</td><td>'+osrelease+'</td><td>'+str(disk)+' GB</td><td>'+role+'</td>'
            table_output += '</tr>'

        table_output += '</tbody>'
        table_output += '</table>'
        table_output += '</br></br>'

    return table_output

if __name__ == '__main__':

   html_body    = ''
   html_body   += '<!DOCTYPE html>'
   html_body   += '<html>'
   html_body   += '<head>'
   html_body   += '<style> table {  font-family: arial, sans-serif; border-collapse: collapse; width: 100%; } td, th { border: 1px solid #dddddd; text-align: left;  padding: 8px; }'
   html_body   += 'tr:nth-child(even) { background-color: #f2f2f2; } th { padding-top: 12px; padding-bottom: 12px; text-align: left; background-color: #039be5; color: white;}'
   html_body   += 'tr:hover {background-color: #ddd;}</style>'
   html_body   += '</head>'
   html_body   += '<body class="wide comments example dt-example-jqueryui">'

   workspace = ''
   try:
        workspace = sys.argv[1]
        salt_report_dir = os.path.join(workspace,'salt_report')
        if not os.path.isdir(salt_report_dir):
           os.makedirs(salt_report_dir)
        html_table  = generate_html_table()
        html_body   += html_table
   except IndexError as error:
        raise
   except OSError as error:
        raise

   html_body   += '</body>'
   html_body   += '</html>'

   f = open(os.path.join(workspace,'salt_report','index.html'), "w")
   f.write(html_body)
   f.close()
