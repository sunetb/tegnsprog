package dk.stbn.testts;


/*
logik-klasse som ikke er Appl?
IO-klasse?
data-klasse

div videofejl(
lillebitte (3011), hej (3077)

Fejl:
Net fra, start app: INGEN BESKED (pga. nystartet-flag)

Features:

0.1.3 Håndtering af svigtende netforbindelse
0.1.4 Fuld artikel animation (design: mock-up)
0.1.5 Forsøg på automatisk detektering af fejl i videoafspilning og skift af format (webm/mp4). Fund linie 101 virker ikke. Men skift format i lin 100
0.1.6 liggende vising (design)
0.1.7 Fuld artikel (design)
0.1.8 Liggende visning implementeret
0.1.9 Søgning med *
0.2.0 Beta: Fuld artikel implementeret


Andet:
-evt ikke pause/play ved scroll
-Forcer testtilstand i Test-akt

Nice to have:
    -Hold og før horisontalt på video scroller i pausede frames
    -Afspil alle automatisk, til/fra. Evt i indstilling
    -Seekbar
    -caching af fund


%%%%%%%%%%%%%%%%%%%%%%%%%%Brugertest%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


Når jeg søger med * , får jeg intet resultat.



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%Crshes
 libc  F  Fatal signal 11 (SIGSEGV), code 1, fault addr 0x0 in
                             tid 13712 (dummySurface)
                  DEBUG  F  #00 pc 000038c5  /vendor/lib/libOpenglSystemCommon.s
                            o (_ZN14HostConnection17getWithThreadInfoEP13EGLThre
                            adInfo+213)
                         F  #01 pc 000037e4  /vendor/lib/libOpenglSystemCommon.s
                            o (_ZN14HostConnection3getEv+52)
                         F  #02 pc 0000e3b8  /vendor/lib/egl/libEGL_emulation.so
                             (eglChooseConfig+456)
                         F  #03 pc 0000c4ec  /system/lib/libEGL.so (eglChooseCon
                            fig+556)
                         F  #04 pc 0008a904  /system/lib/libandroid_runtime.so (
                            _ZL23android_eglChooseConfigP7_JNIEnvP8_jobjectS2_P1
                            0_jintArrayiP13_jobjectArrayiiS4_i+692)
                         F  #05 pc 00e72f4f  /system/framework/x86/boot-framewor
                            k.oat (offset 0x5e6000) (android.opengl.EGL14.eglCho
                            oseConfig+303)
                         F  #06 pc 00638ea2  /system/lib/libart.so (art_quick_in
                            voke_static_stub+418)
                         F  #07 pc 00112b92  /system/lib/libart.so (_ZN3art9ArtM
                            ethod6InvokeEPNS_6ThreadEPjjPNS_6JValueEPKc+306)
                         F  #08 pc 003231ff  /system/lib/libart.so (_ZN3art11int
                            erpreter34ArtInterpreterToCompiledCodeBridgeEPNS_6Th
                            readEPNS_9ArtMethodEPKNS_7DexFile8CodeItemEPNS_11Sha
                            dowFrameEPNS_6JValueE+367)
                         F  #09 pc 0031cec3  /system/lib/libart.so (_ZN3art11int
                            erpreter6DoCallILb1ELb0EEEbPNS_9ArtMethodEPNS_6Threa
                            dERNS_11ShadowFrameEPKNS_11InstructionEtPNS_6JValueE
                            +803)
                         F  #10 pc 0062182d  /system/lib/libart.so (MterpInvokeS
                            taticRange+397)
                         F  #11 pc 00629d21  /system/lib/libart.so (artMterpAsmI
                            nstructionStart+15265)
                         F  #12 pc 002f5f59  /system/lib/libart.so (_ZN3art11int
                            erpreterL7ExecuteEPNS_6ThreadEPKNS_7DexFile8CodeItem
                            ERNS_11ShadowFrameENS_6JValueEb+537)
                         F  #13 pc 002fdeda  /system/lib/libart.so (_ZN3art11int
                            erpreter33ArtInterpreterToInterpreterBridgeEPNS_6Thr
                            eadEPKNS_7DexFile8CodeItemEPNS_11ShadowFrameEPNS_6JV
                            alueE+234)
                         F  #14 pc 0031bdb5  /system/lib/libart.so (_ZN3art11int
                            erpreter6DoCallILb0ELb0EEEbPNS_9ArtMethodEPNS_6Threa
                            dERNS_11ShadowFrameEPKNS_11InstructionEtPNS_6JValueE
                            +773)
                         F  #15 pc 0061f80b  /system/lib/libart.so (MterpInvokeD
                            irect+523)
                         F  #16 pc 006299a1  /system/lib/libart.so (artMterpAsmI
                            nstructionStart+14369)
                         F  #17 pc 002f5f59  /system/lib/libart.so (_ZN3art11int
                            erpreterL7ExecuteEPNS_6ThreadEPKNS_7DexFile8CodeItem
                            ERNS_11ShadowFrameENS_6JValueEb+537)
                         F  #18 pc 002fddbb  /system/lib/libart.so (_ZN3art11int
                            erpreter30EnterInterpreterFromEntryPointEPNS_6Thread
                            EPKNS_7DexFile8CodeItemEPNS_11ShadowFrameE+139)
                         F  #19 pc 0060e53f  /system/lib/libart.so (artQuickToIn
                            terpreterBridge+1375)
                         F  #20 pc 0063ed2d  /system/lib/libart.so (art_quick_to
                            _interpreter_bridge+77)
                         F  #21 pc 0008f617  /dev/ashmem/dalvik-jit-code-cache (
                            deleted)
                         F  #22 pc 000a55c7  /dev/ashmem/AshmemAllocator_hidl (d
                            eleted)
I think I've solved this problem, my solution is : make sure the background encoder thread's opengl operation finished then surfaceTexture in GLSurfaceView Renderer onDrawFrame can updateTexImage. my opinion, textureIds are used in multithread environment, we must make sure the operation on texture do not in race condition

*/
public class Noter
{

}
