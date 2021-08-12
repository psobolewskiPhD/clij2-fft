from setuptools import setup, find_packages

setup(name='clij2-fft',
      version='0.1',
      description='A python wrapper around clij2 opencl FFT algorithms',
      url='https://github.com/clij/clij2-fft',
      author='Robert Haase, Brian Northan',
      author_email='bnorthan@gmail.com',
      license='BSD',
      packages=find_packages(),
      install_requires=['dask','dask-image'],
      data_files=[('',['lib/win64/clij2fft.dll','lib/win64/clFFT.dll', 'lib/linux64/libclij2fft.so', 'lib/linux64/libclFFT.so.2'])],
      zip_safe=False)
