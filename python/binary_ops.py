import os
import io


# split
def split_binary_file(file_name, chunk_size=10000, output_dir="/"):
    file_num = 0

    print("splitting {} into {} files of size {} MB...".format(file_name, os.path.getsize(file_name)//chunk_size, chunk_size/1e6))

    with open(file_name, 'rb') as f:
        chunk = f.read(chunk_size)
        while chunk:
            with open(output_dir + "chunk" + str(file_num), 'wb') as chunk_file:
                chunk_file.write(chunk)
            file_num += 1
            chunk = f.read(chunk_size)

    print("finished splitting operation")


# join binary files, return file handle
def join_binary_files(directory, output_file):
    # files = [directory+"chunk"+str(i) for i in range(8)]
    # print(files)

    out_data = b''
    print("joining files in " + directory)
    for file in os.scandir(directory):
        if not file.is_dir():
            with open(file, 'rb') as f:
                out_data += f.read()

    file_handle = io.BytesIO(out_data)
    print(type(file_handle.read()))

    # with open(output_file, 'wb') as f:
    #     f.write(out_data)

    print("done joining file ")
    return file_handle


if __name__ == '__main__':
    join_binary_files("static/resources/glove/split/", "static/resources/glove/glove.gz.vectors.npz")
    # split_binary_file("static/resources/glove/glove.gz.vectors.npz", 10000000, "static/resources/glove/split/")
    pass
