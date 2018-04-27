load %COVERAGE_FILE%

# standard pymol style
bg_color white
unset depth_cue
set ray_opaque_background, off
set ray_trace_mode, 1
space cmyk
set_color redNegative, [222,135,135]
set_color greenPositive, [171,200,55]
set_color gray80, [51,51,51]
set_color yellowNeutral, [255,238,170]
set antialias, 6
set valence, 1
spectrum b, white_red
as cartoon
cartoon putty
show nb_spheres

select hyb, resn hyb
color tv_blue, hyb

select pis, resn pis
color tv_orange, pis

select pic, resn pic
color tv_red, pic

select sab, resn sab
color tv_yellow, sab

select hal, resn hal
color hotpink, hal

select mec, resn mec
color chocolate, mec
