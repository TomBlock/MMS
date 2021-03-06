from workflow import Workflow

w = Workflow('usecase17_mhs_n19_amsub_n16', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('mhs-n19', '2009-04-01', '2014-06-05', 'v1.0')
w.add_secondary_sensor('amsub-n16', '2009-04-01', '2014-06-05', 'v1.0')

w.set_usecase_config('usecase-17.xml')

w.run_matchup(hosts=[('localhost', 72)])