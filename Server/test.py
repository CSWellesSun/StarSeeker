from http import client
import socket
import json
import imufusion
import numpy
import astropy
from astropy.io import ascii
from astropy.coordinates import EarthLocation, SkyCoord, AltAz
from astropy import units


# 根据输入的天体HR编号返回天体的RA与DES(北极星的HR编号是424)
# 返回格式第一个项是RA, 第二个项是DES
def get_ra_and_des(hr_num):
    # 读取文件提取出对应行
    table = ascii.read("catalog", readme="ReadMe")
    table.add_index('HR')
    row = table.loc[hr_num]
    # 提取查询得到的RA和DES
    ret = []
    ret.append(str(row[table.index_column('RAh')])+'h'+str(row[table.index_column('RAm')])+'m'+str(row[table.index_column('RAs')])+'s')
    ret.append(str(row[table.index_column('DEd')])+'d'+str(row[table.index_column('DEm')])+'m'+str(row[table.index_column('DEs')])+'s')
    return ret


# 根据输入天体的RA/DES, 海拔, 经度纬度与时间计算天体的地平坐标系坐标(北极星的HR编号是424)
# 参数格式longitude和latitude均为角度, 东经为正,西经为负
# 返回格式是第一个项是ALT, 第二个项是AZ
def get_skycoord(ra_des, longitude, latitude, height, time):
    # 构建观测者
    observer = EarthLocation(lat=latitude*units.deg, lon=longitude*units.deg, height=height*units.m)
    # 构建目标天体
    opt_target = SkyCoord(ra=ra_des[0], dec=ra_des[1], unit=(units.hourangle, units.deg))
    astropy.utils.iers.conf.auto_download = False
    astropy.utils.iers.conf.remote_timeout = 0.0
    astropy.utils.iers.conf.iers_degraded_accuracy = "ignore"
    astropy.utils.iers.conf.auto_max_age = None
    opt_pos = opt_target.transform_to(AltAz(obstime=time, location=observer))
    ret = [opt_pos.alt.deg, opt_pos.az.deg]
    return ret


def get_opt_altaz(hr_num, longitude, latitude, height, time):
    ra_des = get_ra_and_des(hr_num)
    return get_skycoord(ra_des, longitude, latitude, height, time)


# 规定格式:
# opt_alzat, 单位为角度,直接获得
# gyro 长度为10的三维numpy.array, 顺序为x, y, z
# acc 长度为10的三维numpy.array, 顺序为x, y, z
# 返回值为int类型, 范围为[0, 255]
def get_diff_level(opt_altaz, gyro, acc, ahrs=imufusion.Ahrs()):
    for i in range(0, 25):
        ahrs.update_no_magnetometer(gyro[i, : ], acc[i, : ], 1 / 250)
    head_euler = ahrs.quaternion.to_euler()

    # print(head_euler)

    # head_euler[2] = yaw, head_euler[1] = pitch
    distance = numpy.arccos(numpy.cos(opt_altaz[0])*numpy.cos(head_euler[1])*numpy.cos(opt_altaz[1]-head_euler[2])+numpy.sin(opt_altaz[0])*numpy.sin(head_euler[0]))
    try:
        ret_value = int(abs(distance) / numpy.pi * 255)
    except ValueError:
        print("ValueError Exception RAISED")
        return 0
    else:
        return ret_value


#创建一个socket对象
socket_server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
host = "192.168.217.30"
port = 9999
#绑定地址
socket_server.bind((host, port))
#设置监听
socket_server.listen(5)
# socket_server.accept()返回一个元组, 元素1为客户端的socket对象, 元素2为客户端的地址(ip地址，端口号)
client_socket, address = socket_server.accept()

flag = 1
except_flag = 0
opt_altaz = []
ret_value = 0

#while循环是为了让对话持续
while True:
	# 接收客户端的请求
    recvmsg = client_socket.recv(2048)
    # 把接收到的数据进行解码
    strData = recvmsg.decode("utf-8")

    try:
        Data = json.loads(strData)
    except json.JSONDecodeError:
        print("Not Valide JSON data, return last ret_value")
        except_flag = 1


    # 设置退出条件
    if strData == '':
        client_socket, address = socket_server.accept()
    elif except_flag == 0:
        # print("Recieve:",Data)
        print(end="")
    else:
        print("Not Valide JSON data, EXCEPTION RAISED")
    

    if except_flag == 1:
        msg = str(ret_value) + '\n'
        client_socket.send(msg.encode("utf-8"))
        except_flag = 0
    else:
        # 输入
        if flag:
            opt_altaz = get_opt_altaz(424, 120.15, 30.25, 19.0, Data['time'])
            flag = 0
        # 接收数据处理

        # 处理陀螺仪数据
        pro_gyro = numpy.zeros((25, 3))
        for rows in range(25):
            for i in range(3):
                pro_gyro[rows][i] = Data['gyroData'][rows][i] / 4096
        # 处理加速度数据
        pro_acc = numpy.zeros((25, 3))
        for rows in range(25):
            for i in range(3):
                pro_acc[rows][i] = Data['accelData'][rows][i] / 16.384

        # print(pro_gyro)
        # print(pro_acc)
        ret_value = get_diff_level(opt_altaz, pro_gyro, pro_acc)
        print(ret_value)

        msg = str(ret_value) + '\n'
        #发送数据，需要进行编码
        client_socket.send(msg.encode("utf-8"))
#关闭服务器端
socket_server.close()

