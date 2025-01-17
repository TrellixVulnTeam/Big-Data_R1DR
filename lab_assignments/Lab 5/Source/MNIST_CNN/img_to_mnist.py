import os
from PIL import Image
from array import *
from random import shuffle
import glob

# Load from and save to
Names = [['./training_images', 'train'], ['./test_images', 'test']]

for name in Names:
    print('Processing ', name[0])
    data_image = array('B')
    data_label = array('B')

    FileList = []
    for dirname in os.listdir(name[0]):
        path = os.path.join(name[0], dirname)
        for filename in os.listdir(path):
            if filename.endswith(".jpg"):
                FileList.append(os.path.join(name[0], dirname, filename))

    shuffle(FileList)  # Useful for further segmenting the validation set

    for filename in FileList:

        label = int(filename.split('\\')[1])

        Im = Image.open(filename).resize((28, 28)).convert('L')
        # Im = Image.open(filename).convert('L')
        #Im.show()
        pixel = Im.load()

        width, height = Im.size
        for x in range(0, width):
            for y in range(0, height):
                try:
                    data_image.append(pixel[y, x])
                except IndexError:
                    print("Error at:", y, x)
                    quit()

        data_label.append(label)  # labels start (one unsigned byte each)

    hexval = "{0:#0{1}x}".format(len(FileList), 6)  # number of files in HEX
    # header for label array
    header = array('B')
    header.extend([0, 0, 8, 1, 0, 0])
    header.append(int('0x' + hexval[2:][:2], 16))
    header.append(int('0x' + hexval[2:][2:], 16))

    data_label = header + data_label

    # additional header for images array

    if max([width, height]) <= 256:
        header.extend([0, 0, 0, width, 0, 0, 0, height])
    else:
        raise ValueError('Image exceeds maximum size: 256x256 pixels');

    header[3] = 3  # Changing MSB for image data (0x00000803)

    data_image = header + data_image

    output_file = open(name[1] + '-images-idx3-ubyte', 'wb')
    data_image.tofile(output_file)
    output_file.close()

    output_file = open(name[1] + '-labels-idx1-ubyte', 'wb')
    data_label.tofile(output_file)
    output_file.close()

# gzip resulting files
print('Compressing files with gzip')
for name in Names:
    os.system('gzip ' + name[1] + '-images-idx3-ubyte')
    os.system('gzip ' + name[1] + '-labels-idx1-ubyte')

print('Deleting old files')
os.chdir('MNIST_data')
files = glob.glob('./*')
for file in files:
    os.remove(file)

print('Moving new files to MNIST_data folder')
os.chdir('../')
files = glob.glob('./*.gz')
for file in files:
    os.rename(file, 'MNIST_data\\'+file[2:])

os.chdir('./MNIST_data')
files = glob.glob('./*.gz')
for file in files:
    if 'test' in file:
        nfile = file.replace('test', 't10k', 1)
        os.rename(file, nfile)
