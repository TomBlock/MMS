from period import Period
from workflow import Workflow

period = Period('2007-05-29', '2015-12-31')
w = Workflow('post_process_uc03_avhrr_n18_iasi_ma', 7, '/group_workspaces/cems2/esacci_sst/mms_new/config', period)

w.set_input_dir('/group_workspaces/cems2/fiduceo/Data/mms/mmd/mmd03/avhrr_n18_iasi_ma')
w.set_usecase_config('usecase-03-pp.xml')

w.run_post_processing(hosts=[('localhost', 48)])