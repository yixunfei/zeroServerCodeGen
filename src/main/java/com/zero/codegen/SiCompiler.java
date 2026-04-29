package com.zero.codegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SiCompiler {
    public static void main(String[] args) throws Exception{
        Map<String,String> arg=parseArgs(args);
        List<String> inputs=parseInputs(arg.getOrDefault("--input",""));
        String out=arg.getOrDefault("--out","generated-src");
        String pkg=arg.getOrDefault("--pkg","com.zero.protocol");
        String protoId=arg.getOrDefault("--protoId","");
        boolean genJava=getBooleanArg(arg, "--genJava", true);
        boolean genCs=getBooleanArg(arg, "--genCs", false);
        String outCs=arg.getOrDefault("--outCs", "cs-src");
        String javaCommonOut=arg.getOrDefault("--javaCommonOut", out);
        String csCommonOut=arg.getOrDefault("--csCommonOut", outCs);
        String boOut=arg.getOrDefault("--boOut", out);
        String boPkg=arg.getOrDefault("--boPkg", pkg+".bo");
        String csNs=arg.getOrDefault("--csNs", pkg);
        boolean genBoImpl=getBooleanArg(arg, "--genBoImpl", false);
        boolean implWithComponent=getBooleanArg(arg, "--implWithComponent", false);
        boolean genAutoConfig=getBooleanArg(arg, "--genAutoConfig", false);
        boolean scanImplPackage=getBooleanArg(arg, "--scanImplPackage", getBooleanArg(arg, "--scanImpl", false));
        boolean simd=getBooleanArg(arg, "--simd", false);  // SIMD闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；婵炴垟鎳為崶顒佸仺缂佸瀵ч悗顒勬倵楠炲灝鍔氭い锔诲灣缁牏鈧綆鍋佹禍婊堟煙閺夊灝顣抽柟顔笺偢閺屽秷顧侀柛鎾寸缁绘稒绻濋崶褏鐣哄┑掳鍊曢幊鎰暤娓氣偓閺屾盯鈥﹂幋婵囩亪婵犳鍠栨鎼佲€旈崘顔嘉ч煫鍥ㄦ尵濡诧綁姊洪幖鐐插婵炲鐩幃楣冩偪椤栨ü姹楅梺鍦劋閸ㄥ綊鏁嶅鍫熲拺缂備焦锚婵洦銇勯弴銊ュ籍鐎规洏鍨介弻鍡楊吋閸℃ぞ鐢绘繝鐢靛Т閿曘倝宕幘顔肩煑闁告洦鍨遍悡蹇涙煕閳╁喚娈旈柡鍡悼閳ь剝顫夊ú蹇涘礉鎼淬劌鐒垫い鎺嶈兌閳洟鎳ｉ妶澶嬬厵闁汇値鍨奸崵娆愩亜椤忓嫬鏆ｅ┑鈥崇埣瀹曞崬鈻庤箛锝嗘缂傚倸鍊风粈渚€顢栭崱娑樼闁告挆鍐ㄧ亰婵犵數濮甸懝鍓х矆閸垺鍠愬鑸靛姇绾惧鏌熼崜褏甯涢柛瀣剁節閺屸剝寰勭€ｉ潧鍔屽┑鈽嗗亜閻倸顫忓ú顏勪紶闁靛鍎涢敐鍡欑闁告瑥顦遍惌鎺楁煙瀹曞洤浠遍柡灞芥椤撳ジ宕卞Δ渚囧悑闂傚倷绶氬褔鎮ч崱妞曟椽濡搁埡鍌涙珫濠电姴锕ら悧濠囧煕閹达附鈷戞い鎰╁€曟禒婊堟煠濞茶鐏￠柡鍛埣椤㈡岸鍩€椤掑嫬钃熼柨婵嗩槹閺呮煡鏌涢埄鍐噮闁汇倕瀚伴幃妤冩喆閸曨剛顦梺鍝ュУ閻楃娀濡存担鑲濇棃宕ㄩ鐙呯床婵犳鍠楅敃鈺呭礈濞戙埄鏁婇柛銉墯閳锋帒霉閿濆洨鎽傞柛銈嗙懄閹便劌顫滈崼銏㈡殼闂佹寧绻勯崑鐐差嚗閸曨垰绠涙い鎺戝亞閸熷洭姊绘担绋挎毐闁圭⒈鍋婇獮濠冩償閿濆洨骞撳┑掳鍊曢幊蹇涙偂濞戞埃鍋撻獮鍨姎濡ょ姵鎮傞悰顕€寮介銈囷紲闂佺粯锕㈠褔鍩㈤崼銉︾厸鐎光偓閳ь剟宕伴弽顓犲祦鐎广儱顦介弫濠勭棯閹峰矂鍝烘慨锝咁樀濮婄粯鎷呴崨濠冨創闂佺懓鍢查澶婄暦濠婂喚娼╅弶鍫涘妼鎼村﹤鈹戦悙鏉戠仧闁搞劌婀辩划濠氭晲閸℃瑧顔曢梺绯曞墲椤ㄥ牏绮婚崘瑁佸綊鎮╅懡銈囨毇濠?
        boolean cleanGeneratedOutputs=getBooleanArg(arg, "--cleanGeneratedOutputs", false);
        boolean advancedMode=inputs.size()!=1
                || !protoId.isBlank()
                || arg.containsKey("--genJava")
                || arg.containsKey("--genCs")
                || arg.containsKey("--outCs")
                || arg.containsKey("--javaCommonOut")
                || arg.containsKey("--csCommonOut")
                || arg.containsKey("--boOut")
                || arg.containsKey("--boPkg")
                || arg.containsKey("--csNs")
                || arg.containsKey("--genBoImpl")
                || arg.containsKey("--implWithComponent")
                || arg.containsKey("--genAutoConfig")
                || arg.containsKey("--scanImplPackage")
                || arg.containsKey("--scanImpl")
                || arg.containsKey("--simd")
                || arg.containsKey("--cleanGeneratedOutputs");

        if(inputs.isEmpty()) throw new IllegalArgumentException("missing --input");
        if(!genJava && !genCs) throw new IllegalArgumentException("at least one target must be enabled");
        List<String> warns;
        if(!advancedMode && genJava && !genCs){
            warns=compile(inputs.get(0), out, pkg, javaCommonOut);
        }else{
            warns=compileBatch(inputs, out, pkg, protoId, genJava, genCs, outCs, javaCommonOut, csCommonOut, boOut, boPkg, csNs,
                    genBoImpl, implWithComponent, genAutoConfig, scanImplPackage, simd, cleanGeneratedOutputs);
        }

        if(!warns.isEmpty()){
            System.err.println("SI compile warnings:");
            for(String w: warns) System.err.println("- "+w);
        }
    }
    public static List<String> compile(String in, String out, String pkg) throws Exception{
        return compile(in, out, pkg, out);
    }
    public static List<String> compile(String in, String out, String pkg, String javaCommonOut) throws Exception{
        WARNINGS.clear();
        String text=Files.readString(Path.of(in));
        text=preprocess(text);
        String baseRaw=stripExt(Path.of(in).getFileName().toString());
        String base=toCamel(baseRaw);
        if(!base.equals(baseRaw)) addWarn("闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；闁绘劗鍎ら崑瀣煟濡崵婀介柍褜鍏涚欢姘嚕閹绢喖顫呴柍鈺佸暞閻濇牠姊绘笟鈧埀顒傚仜閼活垱鏅堕弶娆剧唵閻熸瑥瀚粈瀣偓瑙勬礈閸忔﹢銆佸鈧幃鈺冨枈婢跺苯绨ラ梻鍌欐祰椤曆囧礄閻ｅ瞼绀婇柛鈩冪☉绾惧鏌熼幑鎰厫妞ゎ偅娲熼弻宥夊传閸曨偀鍋撻懡銈囦笉闁告挆鈧崑鎾绘偡閺夋妫岄梺鍝ュУ濞叉粓鎳炴潏銊ч檮闁告稑锕﹂崢鎼佹⒑閸涘﹣绶遍柛鐘冲哺瀹曪綁鍩€椤掑嫭鈷戦柛婵嗗濠€鎵磼鐎ｎ偄鐏撮柛鈹垮劜瀵板嫰骞囬鍌滃幀婵犵妲呴崹鎶藉储瑜斿畷鐢割敆閸曨兘鎷绘繛杈剧悼閻℃棃宕靛▎寰棃鎮╅搹顐⑩偓鎰版煃閵夘垳鐣遍柣锝忕節閺屽洭鏁傞悾宀€鈻夊┑鐘垫暩閸嬫稑螣婵犲啰顩叉繝闈涚懁婢舵劕閱囬柣鏃囨椤旀洟姊洪悷閭﹀殶闁稿鍋ら幆鍐箣閿旂晫鍘介梺闈涚墕閹冲酣顢旈鐔稿弿濠电姴鎳忛鐘电磼椤旂晫鎳囩€规洜濞€閸╁嫰宕橀埡鍌涚槥婵犵數濮烽弫鎼佸磻濞戞﹩鍤曢柛鎾茬閸ㄦ繈鏌熼幑鎰惞鐎?"+baseRaw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+base);
        List<EnumDef> enums=parseEnums(text);
        List<Struct> structs=parseStructs(text);
        Proto proto=parseProto(text);
        Path outDir=Paths.get(out, pkg.replace('.','/'));
        Files.createDirectories(outDir);
        JavaRuntimeSupport.writeRuntimeSources(javaCommonOut, pkg);
        Codegen.setEnums(enums.stream().map(e->e.name).collect(java.util.stream.Collectors.toSet()));
        Codegen.setStructs(structs);
        for(EnumDef e: enums){
            String code=Codegen.generateEnum(pkg,e);
            writeStringIfChanged(outDir.resolve(e.name+".java"), code);
        }
        for(Struct s: structs){
            String code=Codegen.generateStruct(pkg,s);
            writeStringIfChanged(outDir.resolve(s.name+".java"), code);
        }
        if(proto!=null){
            String boPkg=pkg+".bo";
            String code=Codegen.generateBO(pkg,boPkg,base,proto);
            Path boDir=Paths.get(out, boPkg.replace('.','/'));
            Files.createDirectories(boDir);
            writeStringIfChanged(boDir.resolve("I"+base+"BO.java"), code);
        }
        return new ArrayList<>(WARNINGS);
    }
    public static List<String> compileBatch(List<String> inputs, String out, String pkg, String protoIdPath) throws Exception{
        return compileBatch(inputs, out, pkg, protoIdPath, true, false, "", out, "", out, pkg+".bo", null, false, false, false, false, false, false);
        /*
        Map<String, int[]> idCfg=parseProtoId(protoIdPath, inputs.isEmpty()? null: Paths.get(inputs.get(0)).getParent());
        List<Assign> assigns=new ArrayList<>();
        for(String in: inputs){
            String text=Files.readString(Path.of(in));
            text=preprocess(text);
            String baseRaw=stripExt(Path.of(in).getFileName().toString());
            String nameLower=baseRaw.toLowerCase();
            String base=toCamel(baseRaw);
            if(!base.equals(baseRaw)) addWarn("闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；闁绘劗鍎ら崑瀣煟濡崵婀介柍褜鍏涚欢姘嚕閹绢喖顫呴柍鈺佸暞閻濇牠姊绘笟鈧埀顒傚仜閼活垱鏅堕弶娆剧唵閻熸瑥瀚粈瀣偓瑙勬礈閸忔﹢銆佸鈧幃鈺冨枈婢跺苯绨ラ梻鍌欐祰椤曆囧礄閻ｅ瞼绀婇柛鈩冪☉绾惧鏌熼幑鎰厫妞ゎ偅娲熼弻宥夊传閸曨偀鍋撻懡銈囦笉闁告挆鈧崑鎾绘偡閺夋妫岄梺鍝ュУ濞叉粓鎳炴潏銊ч檮闁告稑锕﹂崢鎼佹⒑閸涘﹣绶遍柛鐘冲哺瀹曪綁鍩€椤掑嫭鈷戦柛婵嗗濠€鎵磼鐎ｎ偄鐏撮柛鈹垮劜瀵板嫰骞囬鍌滃幀婵犵妲呴崹鎶藉储瑜斿畷鐢割敆閸曨兘鎷绘繛杈剧悼閻℃棃宕靛▎寰棃鎮╅搹顐⑩偓鎰版煃閵夘垳鐣遍柣锝忕節閺屽洭鏁傞悾宀€鈻夊┑鐘垫暩閸嬫稑螣婵犲啰顩叉繝闈涚懁婢舵劕閱囬柣鏃囨椤旀洟姊洪悷閭﹀殶闁稿鍋ら幆鍐箣閿旂晫鍘介梺闈涚墕閹冲酣顢旈鐔稿弿濠电姴鎳忛鐘电磼椤旂晫鎳囩€规洜濞€閸╁嫰宕橀埡鍌涚槥婵犵數濮烽弫鎼佸磻濞戞﹩鍤曢柛鎾茬閸ㄦ繈鏌熼幑鎰惞鐎?"+baseRaw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+base);
            List<EnumDef> enums=parseEnums(text);
            List<Struct> structs=parseStructs(text);
            Proto proto=parseProto(text);
            Path outDir=Paths.get(out, pkg.replace('.','/'));
            Files.createDirectories(outDir);
            Codegen.setEnums(enums.stream().map(e->e.name).collect(java.util.stream.Collectors.toSet()));
            for(EnumDef e: enums){
                String code=Codegen.generateEnum(pkg,e);
                Files.writeString(outDir.resolve(e.name+".java"), code);
            }
            for(Struct s: structs){
                String code=Codegen.generateStruct(pkg,s);
                Files.writeString(outDir.resolve(s.name+".java"), code);
            }
            if(proto!=null){
                String code=Codegen.generateBO(pkg, pkg+".bo", base, proto);
                Path boDir=Paths.get(out, (pkg+".bo").replace('.','/'));
                Files.createDirectories(boDir);
                Files.writeString(boDir.resolve("I"+base+"BO.java"), code);
                int[] pair=idCfg.getOrDefault(nameLower, new int[]{1001,2000});
                int c2sStart = (pair[0]%2==1)? pair[0]: pair[0]+1;
                int s2cStart = (pair[1]%2==0)? pair[1]: pair[1]+1;
                assigns.add(new Assign(base, nameLower, proto.c2s, proto.s2c, c2sStart, s2cStart));
            }
        }
        if(!assigns.isEmpty()){
            Path outDir=Paths.get(out, pkg.replace('.','/'));
            Files.writeString(outDir.resolve("ProtoIds.java"), Codegen.generateIds(pkg, assigns));
            Path boDir=Paths.get(out, (pkg+".bo").replace('.','/'));
            Files.createDirectories(boDir);
            Files.writeString(boDir.resolve("ProtoDispatchManager.java"), Codegen.generateDispatcher(pkg, pkg+".bo", assigns));
        }
        return new ArrayList<>(WARNINGS);
        */
    }
    public static List<String> compileBatch(List<String> inputs, String outProto, String pkgProto, String protoIdPath, boolean genJava, boolean genCs, String outCs, String outBO, String pkgBO, String csNs) throws Exception{
        return compileBatch(inputs, outProto, pkgProto, protoIdPath, genJava, genCs, outCs, outProto, outCs, outBO, pkgBO, csNs, false, false, false, false, false, false);
    }
    public static List<String> compileBatch(List<String> inputs, String outProto, String pkgProto, String protoIdPath, boolean genJava, boolean genCs, String outCs, String outBO, String pkgBO, String csNs, boolean genBoImpl, boolean implWithComponent, boolean genAutoConfig, boolean scanImplPackage, boolean simd) throws Exception{
        return compileBatch(inputs, outProto, pkgProto, protoIdPath, genJava, genCs, outCs, outProto, outCs, outBO, pkgBO, csNs, genBoImpl, implWithComponent, genAutoConfig, scanImplPackage, simd, false);
    }
    public static List<String> compileBatch(List<String> inputs, String outProto, String pkgProto, String protoIdPath, boolean genJava, boolean genCs, String outCs, String javaCommonOut, String csCommonOut, String outBO, String pkgBO, String csNs, boolean genBoImpl, boolean implWithComponent, boolean genAutoConfig, boolean scanImplPackage, boolean simd) throws Exception{
        return compileBatch(inputs, outProto, pkgProto, protoIdPath, genJava, genCs, outCs, javaCommonOut, csCommonOut, outBO, pkgBO, csNs, genBoImpl, implWithComponent, genAutoConfig, scanImplPackage, simd, false);
    }
    public static List<String> compileBatch(List<String> inputs, String outProto, String pkgProto, String protoIdPath, boolean genJava, boolean genCs, String outCs, String javaCommonOut, String csCommonOut, String outBO, String pkgBO, String csNs, boolean genBoImpl, boolean implWithComponent, boolean genAutoConfig, boolean scanImplPackage, boolean simd, boolean cleanGeneratedOutputs) throws Exception{
        return compileBatch(new BatchCompileRequest(
                inputs,
                outProto,
                pkgProto,
                protoIdPath,
                genJava,
                genCs,
                outCs,
                javaCommonOut,
                csCommonOut,
                outBO,
                pkgBO,
                csNs,
                genBoImpl,
                implWithComponent,
                genAutoConfig,
                scanImplPackage,
                simd,
                cleanGeneratedOutputs
        ));
    }
    static List<String> compileBatch(BatchCompileRequest request) throws Exception{
        WARNINGS.clear();
        if(!request.genJava && !request.genCs) throw new IllegalArgumentException("at least one target must be enabled");
        List<ParsedInput> parsedInputs=parseInputs(request.inputs);
        validateParsedInputs(parsedInputs);
        Map<String, int[]> idCfg=parseProtoId(request.protoIdPath, request.guessProtoIdDir());
        Map<String, EnumDef> sharedEnums=mergeEnumDefs(parsedInputs);
        Map<String, Struct> sharedStructs=mergeStructDefs(parsedInputs);
        List<ParsedInput> protocolInputs=mergeProtocolInputs(parsedInputs);
        Codegen.setEnums(new LinkedHashSet<>(sharedEnums.keySet()));
        Codegen.setStructs(sharedStructs.values());
        List<Assign> assigns=buildAssigns(protocolInputs, idCfg);
        BatchCompilePlan plan=new BatchCompilePlan(sharedEnums, sharedStructs, protocolInputs, assigns);
        cleanGeneratedOutputs(request, plan);
        generateJavaOutputs(request, plan);
        generateCSharpOutputs(request, plan);
        return new ArrayList<>(WARNINGS);
        /*
        Map<String, int[]> idCfg=parseProtoId(protoIdPath, inputs.isEmpty()? null: Paths.get(inputs.get(0)).getParent());
        List<Assign> assigns=new ArrayList<>();
        List<Struct> allStructs=new ArrayList<>();
        List<EnumDef> allEnums=new ArrayList<>();
        Map<String,Proto> protoMap=new LinkedHashMap<>();
        for(String in: inputs){
            String text=Files.readString(Path.of(in));
            text=preprocess(text);
            String baseRaw=stripExt(Path.of(in).getFileName().toString());
            String nameLower=baseRaw.toLowerCase();
            String base=toCamel(baseRaw);
            if(!base.equals(baseRaw)) addWarn("闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；闁绘劗鍎ら崑瀣煟濡崵婀介柍褜鍏涚欢姘嚕閹绢喖顫呴柍鈺佸暞閻濇牠姊绘笟鈧埀顒傚仜閼活垱鏅堕弶娆剧唵閻熸瑥瀚粈瀣偓瑙勬礈閸忔﹢銆佸鈧幃鈺冨枈婢跺苯绨ラ梻鍌欐祰椤曆囧礄閻ｅ瞼绀婇柛鈩冪☉绾惧鏌熼幑鎰厫妞ゎ偅娲熼弻宥夊传閸曨偀鍋撻懡銈囦笉闁告挆鈧崑鎾绘偡閺夋妫岄梺鍝ュУ濞叉粓鎳炴潏銊ч檮闁告稑锕﹂崢鎼佹⒑閸涘﹣绶遍柛鐘冲哺瀹曪綁鍩€椤掑嫭鈷戦柛婵嗗濠€鎵磼鐎ｎ偄鐏撮柛鈹垮劜瀵板嫰骞囬鍌滃幀婵犵妲呴崹鎶藉储瑜斿畷鐢割敆閸曨兘鎷绘繛杈剧悼閻℃棃宕靛▎寰棃鎮╅搹顐⑩偓鎰版煃閵夘垳鐣遍柣锝忕節閺屽洭鏁傞悾宀€鈻夊┑鐘垫暩閸嬫稑螣婵犲啰顩叉繝闈涚懁婢舵劕閱囬柣鏃囨椤旀洟姊洪悷閭﹀殶闁稿鍋ら幆鍐箣閿旂晫鍘介梺闈涚墕閹冲酣顢旈鐔稿弿濠电姴鎳忛鐘电磼椤旂晫鎳囩€规洜濞€閸╁嫰宕橀埡鍌涚槥婵犵數濮烽弫鎼佸磻濞戞﹩鍤曢柛鎾茬閸ㄦ繈鏌熼幑鎰惞鐎?"+baseRaw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+base);
            List<EnumDef> enums=parseEnums(text);
            List<Struct> structs=parseStructs(text);
            Proto proto=parseProto(text);
            allEnums.addAll(enums);
            allStructs.addAll(structs);
            if(genJava){
                Path outDir=Paths.get(outProto, pkgProto.replace('.','/'));
                Files.createDirectories(outDir);
                Codegen.setEnums(enums.stream().map(e->e.name).collect(java.util.stream.Collectors.toSet()));
                for(EnumDef e: enums){
                    Files.writeString(outDir.resolve(e.name+".java"), Codegen.generateEnum(pkgProto,e));
                }
                for(Struct s: structs){
                    Files.writeString(outDir.resolve(s.name+".java"), Codegen.generateStruct(pkgProto,s));
                }
                if(proto!=null){
                    Path boDir=Paths.get(outBO, pkgBO.replace('.','/'));
                    Files.createDirectories(boDir);
                    Files.writeString(boDir.resolve("I"+base+"BO.java"), Codegen.generateBO(pkgProto, pkgBO, base, proto));
                    if(genBoImpl){
                        Path implDir=Paths.get(outBO, (pkgBO+".impl").replace('.','/'));
                        Files.createDirectories(implDir);
                        Files.writeString(implDir.resolve(base+"BOImp.java"), Codegen.generateBoImpl(pkgProto, pkgBO, base, proto, implWithComponent));
                    }
                    int[] pair=idCfg.getOrDefault(nameLower, new int[]{1001,2000});
                    int c2sStart = (pair[0]%2==1)? pair[0]: pair[0]+1;
                    int s2cStart = (pair[1]%2==0)? pair[1]: pair[1]+1;
                    assigns.add(new Assign(base, nameLower, proto.c2s, proto.s2c, c2sStart, s2cStart));
                }
            }
            if(proto!=null) protoMap.put(base, proto);
        }
        if(genJava && !assigns.isEmpty()){
            Path outDir=Paths.get(outProto, pkgProto.replace('.','/'));
            Files.writeString(outDir.resolve("ProtoIds.java"), Codegen.generateIds(pkgProto, assigns));
            Path boOutDir=Paths.get(outBO, pkgBO.replace('.','/'));
            Files.createDirectories(boOutDir);
            Files.writeString(boOutDir.resolve("ProtoDispatchManager.java"), Codegen.generateDispatcher(pkgProto, pkgBO, assigns));
            if(genAutoConfig){
                Path cfgDir=Paths.get(outBO, (pkgBO+".config").replace('.','/'));
                Files.createDirectories(cfgDir);
                Files.writeString(cfgDir.resolve("GeneratedProtoAutoConfig.java"), Codegen.generateAutoConfig(pkgProto, pkgBO, assigns, scanImplPackage));
            }
        }
        if(genCs){
            Path csOut=Paths.get(outCs);
            String ns=(csNs!=null && !csNs.isBlank())? csNs : pkgProto;
            Files.createDirectories(csOut);
            Files.writeString(csOut.resolve("BufUtil.cs"), Cs.generateBufUtil(ns));
            for(EnumDef e: allEnums){
                Files.writeString(csOut.resolve(e.name+".cs"), Cs.generateEnum(ns,e));
            }
            for(Struct s: allStructs){
                Files.writeString(csOut.resolve(s.name+".cs"), Cs.generateStruct(ns,s));
            }
            if(!protoMap.isEmpty()){
                Files.writeString(csOut.resolve("ProtoIds.cs"), Cs.generateIds(ns, assigns));
            }
        }
        return new ArrayList<>(WARNINGS);
        */
    }
    static void generateJavaOutputs(BatchCompileRequest request, BatchCompilePlan plan) throws IOException{
        if(!request.genJava) return;
        Path protoDir=packageDir(request.outProto, request.pkgProto);
        Files.createDirectories(protoDir);
        JavaRuntimeSupport.writeRuntimeSources(request.resolveJavaCommonOut(), request.pkgProto);
        Codegen.setStructs(plan.sharedStructs.values());
        Codegen.setSimdEnabled(request.simd);  // set SIMD switch
        for(EnumDef enumDef: plan.sharedEnums.values()){
            writeStringIfChanged(protoDir.resolve(enumDef.name+".java"), Codegen.generateEnum(request.pkgProto, enumDef));
        }
        for(Struct struct: plan.sharedStructs.values()){
            writeStringIfChanged(protoDir.resolve(struct.name+".java"), Codegen.generateStruct(request.pkgProto, struct));
        }
        Path boDir=packageDir(request.outBO, request.pkgBO);
        for(ParsedInput parsed: plan.protocolInputs){
            if(parsed.proto==null) continue;
            Files.createDirectories(boDir);
            writeStringIfChanged(boDir.resolve("I"+parsed.baseCamel+"BO.java"),
                    Codegen.generateBO(request.pkgProto, request.pkgBO, parsed.baseCamel, parsed.proto));
            if(request.genBoImpl){
                Path implDir=packageDir(request.outBO, request.pkgBO+".impl");
                Files.createDirectories(implDir);
                writeStringIfChanged(implDir.resolve(parsed.baseCamel+"BOImp.java"),
                        Codegen.generateBoImpl(request.pkgProto, request.pkgBO, parsed.baseCamel, parsed.proto, request.implWithComponent));
            }
        }
        if(plan.assigns.isEmpty()) return;
        writeStringIfChanged(protoDir.resolve("ProtoIds.java"), Codegen.generateIds(request.pkgProto, plan.assigns));
        Files.createDirectories(boDir);
        writeStringIfChanged(boDir.resolve("ProtoDispatchManager.java"),
                Codegen.generateDispatcher(request.pkgProto, request.pkgBO, plan.assigns));
        if(request.genAutoConfig){
            Path configDir=packageDir(request.outBO, request.pkgBO+".config");
            Files.createDirectories(configDir);
            writeStringIfChanged(configDir.resolve("GeneratedProtoAutoConfig.java"),
                    Codegen.generateAutoConfig(request.pkgProto, request.pkgBO, plan.assigns, request.scanImplPackage));
        }
    }
    static void generateCSharpOutputs(BatchCompileRequest request, BatchCompilePlan plan) throws IOException{
        if(!request.genCs) return;
        String namespace=request.resolveCSharpNamespace();
        String runtimeNamespace=request.resolveCSharpRuntimeNamespace();
        Path csOut=Paths.get(request.outCs);
        Files.createDirectories(csOut);
        CSharpRuntimeSupport.writeRuntimeSources(request.resolveCSharpCommonOut(), runtimeNamespace);
        Codegen.setStructs(plan.sharedStructs.values());
        for(EnumDef enumDef: plan.sharedEnums.values()){
            writeStringIfChanged(csOut.resolve(enumDef.name+".cs"), Cs.generateEnum(namespace, enumDef));
        }
        for(Struct struct: plan.sharedStructs.values()){
            writeStringIfChanged(csOut.resolve(struct.name+".cs"), Cs.generateStruct(namespace, struct, runtimeNamespace));
        }
        if(!plan.assigns.isEmpty()){
            writeStringIfChanged(csOut.resolve("ProtoIds.cs"), Cs.generateIds(namespace, plan.assigns));
        }
    }
    static void cleanGeneratedOutputs(BatchCompileRequest request, BatchCompilePlan plan) throws IOException{
        if(!request.cleanGeneratedOutputs){
            return;
        }
        if(request.genJava){
            deleteFilesWithExtension(packageDir(request.outProto, request.pkgProto), ".java");
            deleteFilesWithExtension(packageDir(request.resolveJavaCommonOut(), JavaRuntimeSupport.runtimeRootPackage(request.pkgProto)), ".java");
            deleteFilesWithExtension(packageDir(request.outBO, request.pkgBO), ".java");
            if(request.genBoImpl){
                deleteFilesWithExtension(packageDir(request.outBO, request.pkgBO+".impl"), ".java");
            }
            if(request.genAutoConfig){
                deleteFilesWithExtension(packageDir(request.outBO, request.pkgBO+".config"), ".java");
            }
        }
        if(request.genCs){
            deleteFilesWithExtension(Paths.get(request.outCs), ".cs");
            Path csCommonOut=Paths.get(request.resolveCSharpCommonOut());
            if(!csCommonOut.equals(Paths.get(request.outCs))){
                deleteFilesWithExtension(csCommonOut, ".cs");
            }
        }
    }
    static void deleteFilesWithExtension(Path dir, String extension) throws IOException{
        if(dir==null || !Files.exists(dir)){
            return;
        }
        try(Stream<Path> stream=Files.walk(dir)){
            for(Iterator<Path> it=stream.iterator(); it.hasNext(); ){
                Path path=it.next();
                if(Files.isRegularFile(path) && path.getFileName().toString().endsWith(extension)){
                    Files.deleteIfExists(path);
                }
            }
        }
    }
    static void writeStringIfChanged(Path path, String content) throws IOException{
        Path parent=path.getParent();
        if(parent!=null){
            Files.createDirectories(parent);
        }
        if(Files.exists(path)){
            String existing=Files.readString(path);
            if(existing.equals(content)){
                return;
            }
        }
        Files.writeString(path, content);
    }
    static Path packageDir(String root, String pkg){
        return Paths.get(root, pkg.replace('.','/'));
    }
    static List<String> parseInputs(String raw){
        List<String> list=new ArrayList<>();
        if(raw==null || raw.isBlank()) return list;
        for(String token: raw.split(",")){
            String s=token.trim();
            if(!s.isEmpty()) list.add(s);
        }
        return list;
    }
    static List<ParsedInput> parseInputs(List<String> inputs) throws Exception{
        List<ParsedInput> parsed=new ArrayList<>();
        for(String in: inputs){
            parsed.add(parseInput(in));
        }
        return parsed;
    }
    static boolean getBooleanArg(Map<String,String> args, String key, boolean defaultValue){
        String value=args.get(key);
        if(value==null) return defaultValue;
        if("true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value)) return true;
        if("false".equalsIgnoreCase(value) || "0".equals(value) || "no".equalsIgnoreCase(value)) return false;
        return true;
    }
    static ParsedInput parseInput(String in) throws Exception{
        String text=Files.readString(Path.of(in));
        text=preprocess(text);
        String baseRaw=stripExt(Path.of(in).getFileName().toString());
        String baseCamel=toCamel(baseRaw);
        if(!baseCamel.equals(baseRaw)) addWarn("闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；闁绘劗鍎ら崑瀣煟濡崵婀介柍褜鍏涚欢姘嚕閹绢喖顫呴柍鈺佸暞閻濇牠姊绘笟鈧埀顒傚仜閼活垱鏅堕弶娆剧唵閻熸瑥瀚粈瀣偓瑙勬礈閸忔﹢銆佸鈧幃鈺冨枈婢跺苯绨ラ梻鍌欐祰椤曆囧礄閻ｅ瞼绀婇柛鈩冪☉绾惧鏌熼幑鎰厫妞ゎ偅娲熼弻宥夊传閸曨偀鍋撻懡銈囦笉闁告挆鈧崑鎾绘偡閺夋妫岄梺鍝ュУ濞叉粓鎳炴潏銊ч檮闁告稑锕﹂崢鎼佹⒑閸涘﹣绶遍柛鐘冲哺瀹曪綁鍩€椤掑嫭鈷戦柛婵嗗濠€鎵磼鐎ｎ偄鐏撮柛鈹垮劜瀵板嫰骞囬鍌滃幀婵犵妲呴崹鎶藉储瑜斿畷鐢割敆閸曨兘鎷绘繛杈剧悼閻℃棃宕靛▎寰棃鎮╅搹顐⑩偓鎰版煃閵夘垳鐣遍柣锝忕節閺屽洭鏁傞悾宀€鈻夊┑鐘垫暩閸嬫稑螣婵犲啰顩叉繝闈涚懁婢舵劕閱囬柣鏃囨椤旀洟姊洪悷閭﹀殶闁稿鍋ら幆鍐箣閿旂晫鍘介梺闈涚墕閹冲酣顢旈鐔稿弿濠电姴鎳忛鐘电磼椤旂晫鎳囩€规洜濞€閸╁嫰宕橀埡鍌涚槥婵犵數濮烽弫鎼佸磻濞戞﹩鍤曢柛鎾茬閸ㄦ繈鏌熼幑鎰惞鐎?"+baseRaw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+baseCamel);
        ParsedInput parsed=new ParsedInput();
        parsed.path=Path.of(in);
        parsed.baseRaw=baseRaw;
        parsed.baseKey=baseRaw.toLowerCase(Locale.ROOT);
        parsed.baseCamel=baseCamel;
        parsed.enums=parseEnums(text);
        parsed.structs=parseStructs(text);
        parsed.proto=parseProto(text);
        return parsed;
    }
    static Set<String> collectEnumNames(List<ParsedInput> inputs){
        Set<String> names=new LinkedHashSet<>();
        for(ParsedInput input: inputs){
            for(EnumDef e: input.enums){
                names.add(e.name);
            }
        }
        return names;
    }
    static void validateParsedInputs(List<ParsedInput> inputs){
        Map<String, Proto> protosByBase=new LinkedHashMap<>();
        Map<String, EnumDef> enumsByName=new LinkedHashMap<>();
        Map<String, Struct> structsByName=new LinkedHashMap<>();
        Map<String, Path> protoSources=new LinkedHashMap<>();
        Map<String, Path> typeSources=new LinkedHashMap<>();
        for(ParsedInput input: inputs){
            if(input.proto!=null){
                Proto existingProto=protosByBase.get(input.baseCamel);
                if(existingProto==null){
                    protosByBase.put(input.baseCamel, input.proto);
                    protoSources.put(input.baseCamel, input.path);
                }else if(!sameProtoShape(existingProto, input.proto)){
                    throw new IllegalArgumentException("Conflicting protocol base name after normalization: "
                            +input.baseCamel+" ("+protoSources.get(input.baseCamel)+" vs "+input.path+")");
                }
            }
            for(EnumDef e: input.enums){
                EnumDef existing=enumsByName.get(e.name);
                if(existing==null){
                    enumsByName.put(e.name, e);
                    typeSources.put(e.name, input.path);
                }else if(!sameEnumShape(existing, e)){
                    throw new IllegalArgumentException("Conflicting generated type name: "+e.name+" ("+typeSources.get(e.name)+" vs "+input.path+")");
                }
            }
            for(Struct s: input.structs){
                Struct existing=structsByName.get(s.name);
                if(existing==null){
                    structsByName.put(s.name, copyStruct(s));
                    typeSources.put(s.name, input.path);
                }else if(!sameStructWireShape(existing, s)){
                    throw new IllegalArgumentException("Conflicting generated type name: "+s.name+" ("+typeSources.get(s.name)+" vs "+input.path+")");
                }else{
                    existing.hot |= s.hot;
                    existing.fixed |= s.fixed;
                    existing.inline |= s.inline;
                }
            }
        }
    }
    static Map<String, EnumDef> mergeEnumDefs(List<ParsedInput> inputs){
        Map<String, EnumDef> merged=new LinkedHashMap<>();
        for(ParsedInput input: inputs){
            for(EnumDef e: input.enums){
                merged.putIfAbsent(e.name, e);
            }
        }
        return merged;
    }
    static Map<String, Struct> mergeStructDefs(List<ParsedInput> inputs){
        Map<String, Struct> merged=new LinkedHashMap<>();
        for(ParsedInput input: inputs){
            for(Struct s: input.structs){
                Struct existing=merged.get(s.name);
                if(existing==null){
                    merged.put(s.name, copyStruct(s));
                }else{
                    existing.hot |= s.hot;
                    existing.fixed |= s.fixed;
                    existing.inline |= s.inline;
                }
            }
        }
        return merged;
    }
    static List<ParsedInput> mergeProtocolInputs(List<ParsedInput> inputs){
        Map<String, ParsedInput> merged=new LinkedHashMap<>();
        for(ParsedInput input: inputs){
            if(input.proto==null){
                continue;
            }
            merged.putIfAbsent(input.baseCamel, input);
        }
        return new ArrayList<>(merged.values());
    }
    static boolean sameEnumShape(EnumDef left, EnumDef right){
        return Objects.equals(left.name, right.name) && Objects.equals(left.items, right.items);
    }
    static boolean sameStructWireShape(Struct left, Struct right){
        if(!Objects.equals(left.name, right.name)) return false;
        if(left.fields.size()!=right.fields.size()) return false;
        for(int i=0;i<left.fields.size();i++){
            Field lf=left.fields.get(i);
            Field rf=right.fields.get(i);
            if(!sameFieldShape(lf, rf)){
                return false;
            }
        }
        return true;
    }
    static boolean sameProtoShape(Proto left, Proto right){
        return sameMethodList(left.c2s, right.c2s) && sameMethodList(left.s2c, right.s2c);
    }
    static boolean sameMethodList(List<Method> left, List<Method> right){
        if(left.size()!=right.size()) return false;
        for(int i=0;i<left.size();i++){
            if(!sameMethodShape(left.get(i), right.get(i))){
                return false;
            }
        }
        return true;
    }
    static boolean sameMethodShape(Method left, Method right){
        if(!Objects.equals(left.name, right.name)) return false;
        if(left.params.size()!=right.params.size()) return false;
        for(int i=0;i<left.params.size();i++){
            Field lf=left.params.get(i);
            Field rf=right.params.get(i);
            if(!sameFieldShape(lf, rf)){
                return false;
            }
        }
        return true;
    }
    static boolean sameFieldShape(Field left, Field right){
        return Objects.equals(left.type, right.type)
                && Objects.equals(left.name, right.name)
                && Objects.equals(left.fixedLength, right.fixedLength)
                && left.packed==right.packed
                && left.borrow==right.borrow;
    }
    static Struct copyStruct(Struct source){
        Struct copy=new Struct();
        copy.name=source.name;
        copy.hot=source.hot;
        copy.fixed=source.fixed;
        copy.inline=source.inline;
        for(Field field: source.fields){
            Field cloned=new Field();
            cloned.type=field.type;
            cloned.name=field.name;
            cloned.fixedLength=field.fixedLength;
            cloned.packed=field.packed;
            cloned.borrow=field.borrow;
            copy.fields.add(cloned);
        }
        return copy;
    }
    static List<Assign> buildAssigns(List<ParsedInput> inputs, Map<String, int[]> idCfg){
        List<Assign> assigns=new ArrayList<>();
        Set<Integer> reservedStarts=new HashSet<>();
        for(int[] pair: idCfg.values()){
            reservedStarts.add(pair[0]);
            reservedStarts.add(pair[1]);
        }
        int nextDefaultBase=1000;
        for(ParsedInput input: inputs){
            if(input.proto==null) continue;
            int[] pair=idCfg.get(input.baseKey);
            if(pair==null){
                while(reservedStarts.contains(nextDefaultBase) || reservedStarts.contains(nextDefaultBase+1000)){
                    nextDefaultBase+=2000;
                }
                pair=new int[]{nextDefaultBase, nextDefaultBase+1000};
                reservedStarts.add(pair[0]);
                reservedStarts.add(pair[1]);
                addWarn("缂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤缂嶅﹪寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ょ紓宥咃躬瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁绘劦鍓欓崝銈囩磽瀹ュ拑韬€殿喖顭烽幃銏ゅ礂鐏忔牗瀚介梺璇查叄濞佳勭珶婵犲伣锝夘敊閸撗咃紲闂佺粯鍔﹂崜娆撳礉閵堝洨纾界€广儱鎷戦煬顒傗偓娈垮枛椤兘骞冮姀銈呯閻忓繑鐗楃€氫粙姊虹拠鏌ュ弰婵炰匠鍕彾濠电姴浼ｉ敐澶樻晩闁告挆鍜冪闯濠电偠鎻徊浠嬪箹椤愶妇鈧攱淇婇悙顏勨偓銈夊磻閸曨垁鍥垂椤愶紕绠氶梺姹囧灮椤牏绮堢€ｎ偁浜滈柡宥冨妽閻ㄦ垿鏌ｉ妶鍌氫壕闂傚倸鍊风粈浣圭珶婵犲洤纾婚柛娑卞姸閸濆嫷娼ㄩ柍褜鍓熼妴渚€骞橀幇浣告倯婵犮垼娉涢鍌炲箯?"+input.baseRaw+" 闂?protoId 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；婵炴垟鎳為崶顒佸仺缂佸瀵ч悗顒勬⒑閻熸澘鈷旂紒顕呭灦瀹曟垿骞囬悧鍫㈠幈闂佸綊鍋婇崹鎵閿斿墽纾介柛鎰ㄦ櫆缁€鍐磼缂佹绠撴い鏇樺劦閺屻劎鈧綆鍋嗛崙锟犳⒑闁偛鑻晶顖炴煕閺冣偓閻熴儵鎮鹃悜钘夌闁挎洍鍋撶紒鐘电帛缁绘盯鏁愭惔鈥冲箰缂備降鍔忛褔鈥旈崘顔嘉ч柛娑卞灣椤斿洭鏌ｆ惔銏犲毈闁告挾鍠庨锝夊箮閼恒儲娅滄繝銏ｆ硾椤戝洭宕㈤悽鐢电＝濞达絽澹婇崕蹇涙倶韫囨挻鍤囩€殿喓鍔戦幊锟犲Χ閸モ晪绱查梻浣告惈閸燁偊宕愰幖浣稿嚑婵炴垯鍨洪悡鏇㈡煃閸濆嫷鍎戠紒鈾€鍋撻柣搴ゎ潐濞叉鏁幒妞烩偓锕傚Ω閳轰胶顦板銈嗗姂閸ㄧ粯绂掗銏♀拻濞达絿鐡旈崵娆愭叏濮楀牏鐣甸柨婵堝仦瀵板嫭绻涢幒鎴炴儓妞ゎ厹鍔戝畷鐓庮潩閿濆懍澹曢梺鎸庢礀閸婃悂鏌嬮崶顒佺叆婵犻潧妫欓惌妤冪磼閵娾晜鏁辩紒缁樼箞閹粙妫冨☉妤€鎽嬮梻浣筋潐濡炴寧绂嶉悙宸殫濠电姴鍟伴々鐑芥倵閿濆簼绨芥い鏃€鍔曢—鍐Χ閸℃顫囬梺绋匡攻閻楃姴顕ｉ崘宸叆闁割偅绻勯鎰攽閻戝洨绉甸柛鎾寸懄娣囧﹥绂掔€ｎ偆鍘介梺瑙勫礃濞夋盯寮稿☉娆樻闁绘劕顕晶顒佺箾閻撳海绠荤€规洘绮忛ˇ鎾煥濞戞艾鏋涙慨濠勫劋鐎电厧鈻庨幋鐘橈綁姊洪崨濠勬噧闁哥喐娼欓锝囨嫚濞村顫嶅┑鐐叉閸旀洟宕濋崨瀛樷拺闁绘劘妫勯崝姘舵煟閵堝懏鍠樼€规洘妞介幃娆撴倻濡厧骞楅梻浣筋潐閸庡啿鐣烽鍕劦妞ゆ帊鑳剁粻鐐碘偓娈垮枛椤嘲顕ｉ幘顔藉亜闁惧繗顕栭崯搴ㄦ⒒娴ｈ櫣甯涢柛鏃撶畵瀹曟粌顫濋懜闈涗户闂佸搫鍟悧濠囧煕閹达附鐓曟繛鎴烇公瀹搞儵鏌ｈ箛濠冩珔闁宠鍨块幃娆撳煛娴ｅ嘲顥氶梻鍌氬€风粈渚€骞栭锕€瀚夋い鎺戝€婚惌娆撴煙鏉堥箖妾柛瀣ф櫆缁绘繈妫冨☉鍗炲壈闂佺粯甯掗悘姘跺Φ閸曨垰绠抽柟瀛樼箥娴犺偐绱掗悙顒€鍔ゆい顓犲厴瀵鏁愭径濠勭杸濡炪倖姊婚崢褎淇婂ú顏呪拺缂佸顑欓崕蹇涙煕婵犲倹鍋ョ€殿喖顭峰鎾偄妞嬪海鐛繝鐢靛仦閸ㄥ爼鏁冮埡浼辨椽顢橀悩鐢碉紳婵炶揪绲介幖顐﹀煝閸垻纾奸柣妯虹－婢х數鈧鍠栭…鐑藉箖閵忋垺鍋橀柍銉ュ帠婢规洟姊哄Ч鍥х仾妞ゆ梹鐗犻幃鐐淬偅閸愨晝鍘遍梺瑙勫礃椤斿﹪骞夋ィ鍐╃厸鐎光偓鐎ｎ剛袦濡ょ姷鍋炵敮鎺曠亙闂佸憡鍔︽禍婵喰掗崶顒佲拻濞达絿鐡旈崵鍐煕閻樻剚娈滈柕鍡楀暣瀹曘劍娼忛崜褏鈼ゆ繝鐢靛█濞佳囶敄閹版澘鏋侀柛鎰靛枟閻撳繘鐓崶銊︾閸熸悂姊洪棃娑氬⒈闁革綇绲介～蹇曠磼濡顎撻梺鍛婄☉閿曘儵宕曢幘缁樷拺闁告繂瀚晶閬嶆煕閹捐泛鏋涙鐐叉瀹曠喖顢涢敐鍡樻珫闂備胶绮崝娆忣焽瑜忕划濠囨晝閸屾稓鍘介柟鍏兼儗閸ㄥ磭绮旈悽鍛婄厱闁绘ê纾晶顏堟懚閻愬瓨鍙忔俊鐐额嚙娴滈箖姊洪崫鍕拱闁烩晩鍨堕崹楣冩晝閸屾氨顓哄┑鐘绘涧濞层劍绂嶉幇鐗堚拻濞达絿鐡斿鎰版煟韫囨柨鍝虹€规洘鍔曢埞鎴﹀幢閳哄倻绋佺紓鍌氬€烽悞锕佹懌婵犳鍨遍幐鎶藉蓟閿濆绠ｉ柣蹇旀た娴滄粓鍩㈤幘宕囨殕闁告洦鍏橀幏娲⒑绾懎浜归柛瀣洴閹﹢顢旈崼鐔哄幗闂佺懓鐏濈€氼喚寮ч埀顒勬⒑缂佹ü绶遍柛鐘愁殘閹广垹鈹戠€ｎ亞鍊為悷婊冪箻閹潡鍩€椤掑嫭鈷掑ù锝呮啞閹牊銇勯敃鈧崯鍧楀煝閺冨牆鍗抽柕蹇婃櫆閺呪晜绻濋姀锝呯厫闁告梹鐗犻崺娑㈠箣閿旂晫鍘卞┑鐐村灦閿曨偊寮ㄧ拠宸唵閻犲搫鎼顓㈡煛鐏炲墽娲村┑鈩冪摃椤﹀弶銇勯敐鍛仮闁哄本绋掗幆鏃堫敊闂傛潙鏋堟繝娈垮枛閿曪妇鍒掗鐐茬闁告稑鐡ㄩ崑锟犳⒑椤撱劎鐣辨繛鍫熺箖缁绘繄鍠婂Ο娲绘綉闂佺顑呴崐鍧楀箖閻愮儤鏅濋柛灞炬皑閻ゅ嫰姊洪棃娴ュ牓寮插☉妯荤函婵犵數濮伴崹濂稿春閺嶎剚鎳岄梻浣告惈閹锋垹绱炴担鍓叉綎婵炲樊浜濋崵鎺楁煏閸繃鍣洪柣搴ｅ亾缁绘繈濮€閵忊€虫畬濡炪倖鍨甸ˇ顖烆敋閿濆惟闁冲搫鍊稿▓鐔兼⒑闂堟侗妾х紒鑼跺亹閼鸿鲸绻濆顓涙嫼闂佸憡绻傜€氼剟寮冲▎鎰閻犲泧鍛殼闁芥鍠庨…璺ㄦ崉閾忓湱浼囩紒鐐劤閸氬绌辨繝鍥ч柛鏇ㄥ幖婵′粙姊哄Ч鍥у闁搞劌婀卞Σ鎰板箳閹惧绉堕梺闈涱焾閸庢娊顢栭崒鐐粹拺闂侇偆鍋涢懟顖涙櫠閸撗呯＝鐎广儱鎳忛ˉ銏⑩偓瑙勬礃閸ㄥ灝鐣烽悢纰辨晝闁逞屽墲閵囨劙骞掑┑鍥ㄦ珨闂備線鈧偛鑻晶瀵糕偓娈垮枦椤曆囶敇閸忕厧绶炲┑鐑嗘娇閸斿秹濡甸崟顖氬唨闁靛ě鍛帓闂備礁鎼Λ搴ㄥ磹濠靛绠栨俊銈呭暞閸犲棝鏌涢弴銊ュ妞わ负鍎靛娲濞戞艾顣洪梺瑙勭ゴ閸撴繄绮氭潏銊х瘈闁搞儯鍔岄埀顒冨吹缁辨帒鈽夊鍡楀壉闂佸搫鎳夐弲鐘差潖濞差亜绀傞梻鍌滎棎閸╁懘姊虹粙鍨劉闁绘搫绻濋獮鍐濞戞帗鏂€闂佹悶鍎弲婵嬫儊閸儲鈷戞慨鐟版搐閻忓弶绻涙担鍐插暞濮ｅ嫰姊婚崒娆愮グ婵℃ぜ鍔庣划鍫熺瑹閳ь剟鐛径鎰櫢闁绘灏欓悿鍕⒑闂堟单鍫ュ疾濠婂牆纾婚柕濞炬櫆閻撴洘绻涢幋鐑囧叕鐎规悶鍎茬换娑㈠醇閵忕姳妲愬┑顔硷攻濡炶棄鐣烽妸锔剧瘈闁告劑鍔庡Ο鍌炴⒒娴ｅ憡鍟炲〒姘殜瀹曞綊骞庨挊澶岊槷闂佸壊鍋呭ú姗€鍩涢幋锔藉仯闁搞儺浜滈惃铏圭磼閻樺啿鈻曢柡灞剧〒閳ь剨缍嗛崑鍡涘煀閺囥垺鐓涢柛娑卞幘閸╋綁鏌熼钘夊姢闁伙絾绻堥崺鈧い鎺嶈兌閳绘梻鈧箍鍎遍ˇ浼存偂閺囥垺鐓欓柣鎰靛厵娴犳粓鏌涚€ｎ偅宕岄柡灞剧〒閳ь剨缍嗛崑鍛暦瀹€鍕厵妞ゆ梹鍎抽崢瀵糕偓娈垮枦濞夋盯鍩㈡惔銊﹀€锋い鎺戭槹椤ユ棃姊婚崒姘偓椋庣矆娓氣偓楠炴顭ㄩ崟顒€寮块梻鍌氱墛閼颁粙宕崟鐢靛弳闂佸壊鍋嗛崰搴♀枔閻斿吋鐓涘璺猴功婢ф垿鏌涢弬鍧楀弰婵﹣绮欏畷鐔碱敍濞戞艾骞堟繝鐢靛仦閸ㄥ爼鏁冮锕€绀夐柣鏂垮悑閻?"+pair[0]+"/"+pair[1]);
                nextDefaultBase+=2000;
            }
            assigns.add(new Assign(
                    input.baseCamel,
                    input.baseKey,
                    input.proto.c2s,
                    input.proto.s2c,
                    normalizeC2sStart(pair[0]),
                    normalizeS2cStart(pair[1])
            ));
        }
        validateAssignments(assigns);
        return assigns;
    }
    static int normalizeC2sStart(int value){
        return (value%2==1)? value: value+1;
    }
    static int normalizeS2cStart(int value){
        return (value%2==0)? value: value+1;
    }
    static void validateAssignments(List<Assign> assigns){
        Map<Integer,String> seen=new HashMap<>();
        for(Assign a: assigns){
            int id=a.c2sStart;
            for(Method m: a.c2s){
                registerAssignedId(seen, id, a.baseCamel+"."+m.name);
                id+=2;
            }
            id=a.s2cStart;
            for(Method m: a.s2c){
                registerAssignedId(seen, id, a.baseCamel+"."+m.name);
                id+=2;
            }
        }
    }
    static void registerAssignedId(Map<Integer,String> seen, int id, String name){
        String prev=seen.putIfAbsent(id, name);
        if(prev!=null){
            throw new IllegalArgumentException("Duplicate proto id "+id+" for "+prev+" and "+name+". Please configure distinct ranges in protoId.txt.");
        }
    }
    static Map<String,String> parseArgs(String[] args){
        Map<String,String> m=new HashMap<>();
        for(int i=0;i<args.length;i++){
            if(args[i].startsWith("--")){
                String k=args[i];
                String v=(i+1<args.length && !args[i+1].startsWith("--"))?args[++i]:"true";
                m.put(k,v);
            }
        }
        return m;
    }
    static String preprocess(String t) throws IOException{
        String[] lines=t.split("\\R");
        StringBuilder sb=new StringBuilder();
        for(String line: lines){
            String l=line.replace('\uFF1A',':');
            int c1=l.indexOf("//");
            if(c1>=0) l=l.substring(0,c1);
            l=l.trim();
            if(l.startsWith("#")) continue;
            if(!l.isEmpty()){ sb.append(l).append("\n"); }
        }
        return sb.toString();
    }
    static String stripExt(String n){ int i=n.lastIndexOf('.'); return i>0?n.substring(0,i):n; }
    static class Field{
        String type;
        String name;
        Integer fixedLength;
        boolean packed;
        boolean borrow;
    }
    static class Struct{ String name; boolean hot; boolean fixed; boolean inline; List<Field> fields=new ArrayList<>(); }
    static class Method{ String name; List<Field> params=new ArrayList<>(); }
    static class Proto{ List<Method> c2s=new ArrayList<>(); List<Method> s2c=new ArrayList<>(); }
    static class EnumDef{ String name; List<String> items=new ArrayList<>(); }
    static class ParsedInput{
        Path path;
        String baseRaw;
        String baseKey;
        String baseCamel;
        List<EnumDef> enums=new ArrayList<>();
        List<Struct> structs=new ArrayList<>();
        Proto proto;
    }
    static class BatchCompileRequest{
        final List<String> inputs;
        final String outProto;
        final String pkgProto;
        final String protoIdPath;
        final boolean genJava;
        final boolean genCs;
        final String outCs;
        final String javaCommonOut;
        final String csCommonOut;
        final String outBO;
        final String pkgBO;
        final String csNs;
        final boolean genBoImpl;
        final boolean implWithComponent;
        final boolean genAutoConfig;
        final boolean scanImplPackage;
        final boolean cleanGeneratedOutputs;
        final boolean simd;  // SIMD闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；婵炴垟鎳為崶顒佸仺缂佸瀵ч悗顒勬倵楠炲灝鍔氭い锔诲灣缁牏鈧綆鍋佹禍婊堟煙閺夊灝顣抽柟顔笺偢閺屽秷顧侀柛鎾寸缁绘稒绻濋崶褏鐣哄┑掳鍊曢幊鎰暤娓氣偓閺屾盯鈥﹂幋婵囩亪婵犳鍠栨鎼佲€旈崘顔嘉ч煫鍥ㄦ尵濡诧綁姊洪幖鐐插婵炲鐩幃楣冩偪椤栨ü姹楅梺鍦劋閸ㄥ綊鏁嶅鍫熲拺缂備焦锚婵洦銇勯弴銊ュ籍鐎规洏鍨介弻鍡楊吋閸℃ぞ鐢绘繝鐢靛Т閿曘倝宕幘顔肩煑闁告洦鍨遍悡蹇涙煕閳╁喚娈旈柡鍡悼閳ь剝顫夊ú蹇涘礉鎼淬劌鐒垫い鎺嶈兌閳洟鎳ｉ妶澶嬬厵闁汇値鍨奸崵娆愩亜椤忓嫬鏆ｅ┑鈥崇埣瀹曞崬鈻庤箛锝嗘缂傚倸鍊风粈渚€顢栭崱娑樼闁告挆鍐ㄧ亰婵犵數濮甸懝鍓х矆閸垺鍠愬鑸靛姇绾惧鏌熼崜褏甯涢柛瀣剁節閺屸剝寰勭€ｉ潧鍔屽┑鈽嗗亜閻倸顫忓ú顏勪紶闁靛鍎涢敐鍡欑闁告瑥顦遍惌鎺楁煙瀹曞洤浠遍柡灞芥椤撳ジ宕卞Δ渚囧悑闂傚倷绶氬褔鎮ч崱妞曟椽濡搁埡鍌涙珫濠电姴锕ら悧濠囧煕閹达附鈷戞い鎰╁€曟禒婊堟煠濞茶鐏￠柡鍛埣椤㈡岸鍩€椤掑嫬钃熼柨婵嗩槹閺呮煡鏌涢埄鍐噮闁汇倕瀚伴幃妤冩喆閸曨剛顦梺鍝ュУ閻楃娀濡存担鑲濇棃宕ㄩ鐙呯床婵犳鍠楅敃鈺呭礈濞戙埄鏁婇柛銉墯閳锋帒霉閿濆洨鎽傞柛銈嗙懄閹便劌顫滈崼銏㈡殼闂佹寧绻勯崑鐐差嚗閸曨垰绠涙い鎺戝亞閸熷洭姊绘担绋挎毐闁圭⒈鍋婇獮濠冩償閿濆洨骞撳┑掳鍊曢幊蹇涙偂濞戞埃鍋撻獮鍨姎濡ょ姵鎮傞悰顕€寮介銈囷紲闂佺粯锕㈠褔鍩㈤崼銉︾厸鐎光偓閳ь剟宕伴弽顓犲祦鐎广儱顦介弫濠勭棯閹峰矂鍝烘慨锝咁樀濮婄粯鎷呴崨濠冨創闂佺懓鍢查澶婄暦濠婂喚娼╅弶鍫涘妼鎼村﹤鈹戦悙鏉戠仧闁搞劌婀辩划濠氭晲閸℃瑧顔曢梺绯曞墲椤ㄥ牏绮婚崘瑁佸綊鎮╅懡銈囨毇濠?

        BatchCompileRequest(List<String> inputs, String outProto, String pkgProto, String protoIdPath,
                            boolean genJava, boolean genCs, String outCs, String javaCommonOut, String csCommonOut, String outBO, String pkgBO, String csNs,
                            boolean genBoImpl, boolean implWithComponent, boolean genAutoConfig, boolean scanImplPackage,
                            boolean simd, boolean cleanGeneratedOutputs){
            this.inputs=inputs;
            this.outProto=outProto;
            this.pkgProto=pkgProto;
            this.protoIdPath=protoIdPath;
            this.genJava=genJava;
            this.genCs=genCs;
            this.outCs=outCs;
            this.javaCommonOut=javaCommonOut;
            this.csCommonOut=csCommonOut;
            this.outBO=outBO;
            this.pkgBO=pkgBO;
            this.csNs=csNs;
            this.genBoImpl=genBoImpl;
            this.implWithComponent=implWithComponent;
            this.genAutoConfig=genAutoConfig;
            this.scanImplPackage=scanImplPackage;
            this.simd=simd;
            this.cleanGeneratedOutputs=cleanGeneratedOutputs;
        }
        BatchCompileRequest(List<String> inputs, String outProto, String pkgProto, String protoIdPath,
                            boolean genJava, boolean genCs, String outCs, String javaCommonOut, String csCommonOut, String outBO, String pkgBO, String csNs,
                            boolean genBoImpl, boolean implWithComponent, boolean genAutoConfig, boolean scanImplPackage){
            this(inputs, outProto, pkgProto, protoIdPath, genJava, genCs, outCs, javaCommonOut, csCommonOut, outBO, pkgBO, csNs,
                 genBoImpl, implWithComponent, genAutoConfig, scanImplPackage, false, false);
        }
        Path guessProtoIdDir(){
            return inputs.isEmpty()? null : Paths.get(inputs.get(0)).getParent();
        }
        String resolveJavaCommonOut(){
            return (javaCommonOut!=null && !javaCommonOut.isBlank())? javaCommonOut : outProto;
        }
        String resolveCSharpNamespace(){
            return (csNs!=null && !csNs.isBlank())? csNs : pkgProto;
        }
        String resolveCSharpCommonOut(){
            return (csCommonOut!=null && !csCommonOut.isBlank())? csCommonOut : outCs;
        }
        String resolveCSharpRuntimeNamespace(){
            return CSharpRuntimeSupport.runtimeNamespace(resolveCSharpNamespace());
        }
    }
    static class Assign{
        String baseCamel; String baseLower;
        List<Method> c2s; List<Method> s2c;
        int c2sStart; int s2cStart;
        Assign(String baseCamel,String baseLower,List<Method> c2s,List<Method> s2c,int cs,int ss){
            this.baseCamel=baseCamel; this.baseLower=baseLower; this.c2s=c2s; this.s2c=s2c; this.c2sStart=cs; this.s2cStart=ss;
        }
    }
    static class BatchCompilePlan{
        final Map<String, EnumDef> sharedEnums;
        final Map<String, Struct> sharedStructs;
        final List<ParsedInput> protocolInputs;
        final List<Assign> assigns;

        BatchCompilePlan(Map<String, EnumDef> sharedEnums, Map<String, Struct> sharedStructs,
                         List<ParsedInput> protocolInputs, List<Assign> assigns){
            this.sharedEnums=sharedEnums;
            this.sharedStructs=sharedStructs;
            this.protocolInputs=protocolInputs;
            this.assigns=assigns;
        }
    }
    static Map<String,int[]> parseProtoId(String path, Path guessDir) throws IOException{
        Path p;
        if(path!=null && !path.isBlank()) p=Path.of(path);
        else p= guessDir==null? null: guessDir.resolve("protoId.txt");
        Map<String,int[]> m=new HashMap<>();
        if(p!=null && Files.exists(p)){
            List<String> lines=Files.readAllLines(p);
            for(String line: lines){
                String l=line.trim();
                if(l.isEmpty()||l.startsWith("#")) continue;
                String[] kv=l.split("\\s+");
                if(kv.length>=3){
                    String name=kv[0].toLowerCase();
                    int a=Integer.parseInt(kv[1]);
                    int b=Integer.parseInt(kv[2]);
                    m.put(name,new int[]{a,b});
                }
            }
        }
        return m;
    }
    static List<EnumDef> parseEnums(String text){
        List<EnumDef> list=new ArrayList<>();
        Pattern p=Pattern.compile("enum\\s+(\\w+)\\s*\\{([^}]*)\\}", Pattern.DOTALL);
        Matcher m=p.matcher(text);
        while(m.find()){
            EnumDef e=new EnumDef();
            String raw=m.group(1);
            String camel=toCamel(raw);
            if(!camel.equals(raw)) addWarn("闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；闁绘劗鍎ら崑瀣煟濡崵婀介柍褜鍏涚欢姘嚕閹绢喖顫呴柍鈺佸暞閻濇洜绱撻崒姘偓鐑芥倿閿曚焦鎳屾繝鐢靛仜閹冲酣鎮ч幘鎰佹綎缂備焦蓱婵挳鏌ц箛鎾剁暛闁逞屽墮閿曨亪寮诲☉銏犵閻庨潧鎽滈悾鑲╃磽娓氬洤鏋熼柣鐔叉櫊閻涱噣骞掗幊铏閸┾偓妞ゆ帒鍊搁ˉ姘舵煙缁嬭法鍑圭紒璇叉閺屾洟宕煎┑鍡╀純濠碘剝鍎抽崲鏌ユ箒濠电姴锕ょ€氼噣鎯岄幒妤佺厸鐎光偓鐎ｎ剛鐦堥悗瑙勬礃閿曘垽寮幇鏉垮窛妞ゆ巻鍋撴い銉﹀哺濮婂宕掑▎鎴濆闂佽鍠栭悥鐓庣暦濠靛棭鍚嬮柛銉ｅ妼鎼村﹪姊洪崨濠冨闁搞劍澹嗙划濠氬蓟閵夛妇鍘棅顐㈡搐椤戝懘鍩€椤掍焦绀嬮柣娑卞櫍婵偓闁挎稑瀚鏇㈡⒑閻熼偊鍤熼柛瀣枛楠炲﹪宕ㄧ€涙鍘卞┑顔姐仜閸嬫挸霉濠婂棙纭炬い顐㈢箰鐓ゆい蹇撴媼濡啫鈹戦悙鏉戠伇濡炲瓨鎮傞崺銏ゅ箻缂佹ǚ鎷洪梺鍛婄缚閸庤鲸鐗庨梻浣虹帛椤ㄥ棝銆冩繝鍌ゅ殨闁哄被鍎遍柋鍥煛閸ャ儱濡介柡鍜佷邯濮婃椽鏌呴悙鑼跺濠⒀冪摠椤?"+raw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+camel);
            e.name=camel;
            String body=m.group(2);
            for(String it: body.split(",")){
                String s=it.trim();
                if(s.isEmpty()) continue;
                s=s.replace("}","").trim();
                if(!s.isEmpty()) e.items.add(s);
            }
            list.add(e);
        }
        return list;
    }
    static List<Struct> parseStructs(String text){
        List<Struct> list=new ArrayList<>();
        Pattern p=Pattern.compile("((?:@\\w+\\s+)*)struct\\s+(\\w+)\\s*\\{([\\s\\S]*?)\\}", Pattern.DOTALL);
        Matcher m=p.matcher(text);
        while(m.find()){
            Struct s=new Struct();
            applyStructAnnotations(s, m.group(1));
            String raw=m.group(2);
            String camel=toCamel(raw);
            if(!camel.equals(raw)) addWarn("缂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤缂嶅﹪寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ょ紓宥咃躬瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁绘劦鍓欓崝銈囩磽瀹ュ拑韬€殿喖顭烽弫鎰緞婵犲嫷鍚呴梻浣瑰缁诲倿骞夊☉銏犵缂備焦顭囬崢杈ㄧ節閻㈤潧孝闁稿﹤缍婂畷鎴﹀Ψ閳哄倻鍘搁柣蹇曞仩椤曆勬叏閸屾壕鍋撳▓鍨灍闁瑰憡濞婇獮鍐ㄢ枎瀵版繂婀遍埀顒婄秵娴滄瑦绔熼弴銏♀拺闁告稑锕︾紓姘舵煕鎼淬倖鐝紒瀣槸椤撳吋寰勭€ｎ剙骞愬┑鐘灱濞夋盯鏁冮敃鈧～婵嬪Ω閳哄倻鍘搁梺閫炲苯澧紒鍌涘笧閳ь剨缍嗛崑鍡涘储閽樺鏀介柍钘夋閻忋儲绻涢崪鍐М闁轰礁绉撮濂稿幢閹邦亞鐩庨梻浣瑰缁诲倸螞濞戙垹鐭楅柍褜鍓熷娲传閸曨剚鎷辩紓浣割儐鐢偤骞戦姀鐘斀閻庯綆浜為敍婊冣攽閻樻墠鍫ュ磻閹惧墎纾兼い鏃傚亾閺嗩剚鎱ㄦ繝鍐┿仢婵☆偄鍟埥澶娾枎閹邦厼鈧兘姊虹拠鎻掝劉缁炬澘绉撮…鍨潨閳ь剟銆佸鑸垫櫜闁糕剝鐟ù鍕煟鎼搭垳鍒伴柣蹇斿哺瀵彃鈹戦崱鈺傚瘜闂侀潧鐗嗘鍛婄娴犲鐓曟慨妞诲亾缂佺姵鐗犻悰顕€骞嬮敃鈧崡鎶芥煏韫囧﹥顎嗛柟?"+raw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+camel);
            s.name=camel;
            String body=m.group(3).trim();
            for(String line: body.split(";")){
                String declaration=line.trim();
                if(declaration.isEmpty()) continue;
                Matcher fieldAnnotationMatcher=Pattern.compile("^((?:@\\w+(?:\\([^)]*\\))?\\s+)*)").matcher(declaration);
                String rawFieldAnnotations="";
                if(fieldAnnotationMatcher.find()){
                    rawFieldAnnotations=fieldAnnotationMatcher.group(1);
                    declaration=declaration.substring(fieldAnnotationMatcher.end()).trim();
                }
                String[] typeAndName=splitTypeAndName(declaration);
                if(typeAndName==null) continue;
                Field f=new Field();
                applyFieldAnnotations(f, rawFieldAnnotations);
                String typeRaw=typeAndName[0].trim();
                String nameRaw=typeAndName[1].trim();
                String typeNorm=normalizeTypeToken(typeRaw);
                if(!typeNorm.equals(typeRaw)) addWarn("缂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤缂嶅﹪寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ょ紓宥咃躬瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁绘劦鍓欓崝銈囩磽瀹ュ拑韬€殿喖顭烽弫鎰緞婵犲嫷鍚呴梻浣瑰缁诲倿骞夊☉銏犵缂備焦顭囬崢杈ㄧ節閻㈤潧孝闁稿﹤缍婂畷鎴﹀Ψ閳哄倻鍘搁柣蹇曞仩椤曆勬叏閸屾壕鍋撳▓鍨灍闁瑰憡濞婇獮鍐ㄢ枎瀵版繂婀遍埀顒婄秵娴滄瑦绔熼弴銏♀拺闁告稑锕︾紓姘舵煕鎼淬倖鐝紒瀣槸椤撳吋寰勭€ｎ剙骞愬┑鐘灱濞夋盯鏁冮敃鈧～婵嬪Ω閳哄倻鍘搁梺閫炲苯澧紒鍌涘笧閳ь剨缍嗛崑鍡涘储閽樺鏀介柍钘夋閻忋儲绻涢崪鍐М闁轰礁绉撮濂稿幢閹邦亞鐩庨梻浣瑰缁诲倸螞濞戙垹鐭楅柍褜鍓熷?"+s.name+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤缂嶅﹪寮婚敐澶婄闁挎繂鎲涢幘缁樼厱濠电姴鍊归崑銉╂煛鐏炶濮傜€殿喗鎸抽幃娆徝圭€ｎ亙澹曢悷婊呭鐢帞澹曢崸妤佺厵閻庣數顭堟牎闂佸摜濮甸崝娆撳蓟閿濆憘鏃堝焵椤掑嫭鍋嬮柛鈩冪懅缁犳棃鏌熼悜妯烩拻缁炬儳銈搁弻宥堫檨闁告挻鑹鹃銉╁礋椤撴繃鍕冪紓浣割儏閵囨ɑ绔熼弴銏♀拺缂佸娉曠粻鐗堛亜閿旇鐏＄紒鍌氱У閵堬綁宕橀埞鐐闂傚倷绶￠崑鍡涘磻濞戙垺鍤愭い鏍ㄧ⊕濞呯娀鏌熺紒銏犳灍闁绘挻娲熼弻宥囨喆閸曨偄濮㈡繛瀛樼矌閸嬫挻绌辨繝鍥ㄥ€锋い蹇撳閸嬫捇寮介锝嗘婵犵數濮寸€氼噣鎯岄崱妞尖偓鎺戭潩閿濆懍澹曢柣搴ゎ潐濞叉粓寮繝姘モ偓浣肝旈崨顓狀槹濡炪倖甯掗崐鎼佺嵁閸儲鈷掑ù锝囩摂濞兼劗鈧娲橀敋闁崇粯鏌ㄩ埥澶愬閻樻彃娈ゆ繝鐢靛仦閸ㄥ爼鎮烽姘ｆ灁濞寸姴顑嗛悡鐔兼煙闁箑骞栫紒鎻掝煼閺岋繝宕卞▎蹇庢闂佸搫鏈ú鐔风暦婵傚壊鏁嗛柛灞剧矋濞堟悂姊绘担鍝ユ瀮妞ゆ泦鍥ㄧ厐闁挎繂顦粻鏍ㄧ箾閸℃ɑ鎯勯柡浣稿閵囧嫰骞囬鍏肩€惧┑鐐叉噹閿曘儵骞堥妸锔剧瘈闁告洦鍘肩粭锟犳⒑閻熸澘妲婚柟铏悾鐑筋敃閿曗偓鍞銈嗙墬缁酣藝椤栨粎纾介柛灞捐壘閳ь剟顥撳▎銏ゆ晸閻樻煡妫峰銈嗘磵閸嬫挾鈧娲栫紞濠傜暦婵傜鍗抽柣妯垮皺閵堬箓姊绘担渚敯闁规椿浜炵划濠氬箣閿旇棄鈧嘲鈹戦悩鎻掓殧濞存粍绮撻弻鐔煎传閸曨剦妫炴繛瀛樼矊婢х晫妲愰幘瀛樺闁告繂瀚呴敐澶嬬厽婵°倕鍟埢鍫⑩偓娈垮枛椤攱淇婇懜闈涚窞濠电姴瀚弳銏ゆ⒒娴ｅ憡璐￠柛搴涘€濆畷褰掑垂椤愶絽寮块梺鍝勬川閸犳挾寮ч埀顒€鈹戦悙鑼闁诲繑绻堥幃姗€鏁撻悩宕囧幍闂佸憡绋戦敃銈夊煝閺囩姭鍋撳▓鍨灕妞ゆ泦鍥х叀濠㈣埖鍔曢～鍛存煟濡吋鏆╅柡澶婃啞娣囧﹪鎮欓鍕ㄥ亾閺嶎灛娑㈠礃閵婂孩鐩獮鎰償閿濆洤楠勯梻浣告惈濞层垽宕瑰ú顏勭厱闁硅揪闄勯悡鐘绘煙椤撶喎绗掗柛鏃€宀搁弻锝堢疀鐎ｎ亜濮曢梺闈涙搐鐎氫即寮崘顔肩＜婵炴垶鐟╅弫顏呬繆?"+typeRaw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+typeNorm);
                String nameNorm=toLowerCamel(nameRaw);
                if(!nameNorm.equals(nameRaw)) addWarn("缂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤缂嶅﹪寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ょ紓宥咃躬瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁绘劦鍓欓崝銈囩磽瀹ュ拑韬€殿喖顭烽弫鎰緞婵犲嫷鍚呴梻浣瑰缁诲倿骞夊☉銏犵缂備焦顭囬崢杈ㄧ節閻㈤潧孝闁稿﹤缍婂畷鎴﹀Ψ閳哄倻鍘搁柣蹇曞仩椤曆勬叏閸屾壕鍋撳▓鍨灍闁瑰憡濞婇獮鍐ㄢ枎瀵版繂婀遍埀顒婄秵娴滄瑦绔熼弴銏♀拺闁告稑锕︾紓姘舵煕鎼淬倖鐝紒瀣槸椤撳吋寰勭€ｎ剙骞愬┑鐘灱濞夋盯鏁冮敃鈧～婵嬪Ω閳哄倻鍘搁梺閫炲苯澧紒鍌涘笧閳ь剨缍嗛崑鍡涘储閽樺鏀介柍钘夋閻忋儲绻涢崪鍐М闁轰礁绉撮濂稿幢閹邦亞鐩庨梻浣瑰缁诲倸螞濞戙垹鐭楅柍褜鍓熷?"+s.name+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤缂嶅﹪寮婚敐澶婄闁挎繂鎲涢幘缁樼厱濠电姴鍊归崑銉╂煛鐏炶濮傜€殿喗鎸抽幃娆徝圭€ｎ亙澹曢悷婊呭鐢帞澹曢崸妤佺厵閻庣數顭堟牎闂佸摜濮甸崝娆撳蓟閿濆憘鏃堝焵椤掑嫭鍋嬮柛鈩冪懅缁犳棃鏌熼悜妯烩拻缁炬儳銈搁弻宥堫檨闁告挻鑹鹃銉╁礋椤撴繃鍕冪紓浣割儏閵囨ɑ绔熼弴銏♀拺缂佸娉曠粻鐗堛亜閿旇鐏＄紒鍌氱У閵堬綁宕橀埞鐐闂傚倷绶￠崑鍡涘磻濞戙垺鍤愭い鏍ㄧ⊕濞呯娀鏌熺紒銏犳灍闁绘挻娲熼弻宥囨喆閸曨偄濮㈡繛瀛樼矌閸嬫挻绌辨繝鍥ㄥ€锋い蹇撳閸嬫捇寮介锝嗘婵犵數濮寸€氼噣鎯岄崱妞尖偓鎺戭潩閿濆懍澹曢柣搴ゎ潐濞叉粓寮繝姘モ偓浣肝旈崨顓狀槹濡炪倖甯掗崐鎼佺嵁閸儲鈷掑ù锝囩摂濞兼劗鈧娲橀敋闁崇粯鏌ㄩ埥澶愬閻樻彃娈ゆ繝鐢靛仦閸ㄥ爼鎮烽姘ｆ灁濞寸姴顑嗛悡鐔兼煙闁箑骞栫紒鎻掝煼閺岋繝宕卞▎蹇庢闂佸搫鏈ú鐔风暦婵傚壊鏁嗛柛灞剧矋濞堟悂姊绘担鍝ユ瀮妞ゆ泦鍥ㄧ厐闁挎繂顦粻鏍ㄧ箾閸℃ɑ鎯勯柡浣稿閵囧嫰骞囬鍏肩€惧┑鐐叉噹閿曘儵骞堥妸锔剧瘈闁告洦鍘肩粭锟犳⒑閻熸澘妲婚柟铏悾鐑筋敃閿曗偓鍞銈嗙墬缁酣藝椤栨粎纾介柛灞捐壘閳ь剟顥撳▎銏ゆ晸閻樻煡妫峰銈嗘磵閸嬫挾鈧娲栫紞濠傜暦婵傜鍗抽柣妯垮皺閵堬箓姊绘担渚敯闁规椿浜炵划濠氬箣閿旇棄鈧嘲鈹戦悩鎻掓殧濞存粍绮嶉妵鍕箳閹搭厽笑婵犫拃灞界仸闁哄矉缍侀崺鈩冪節閸屾粈鍝楅梻浣风串缁蹭粙宕查弻銉稏婵犲﹤鐗嗛悞鍨亜閹哄秵顦风紒璇叉閺岀喖姊荤€靛壊妲銈庡亜閹虫﹢寮诲澶嬪癄濠㈣泛顑愬Λ锛勭磽閸屾氨孝闁兼椿鍨堕崺鐐哄箣閿旇棄浜归梺鍓茬厛閸嬪懎袙閸曨垱鈷戠紒瀣儥閸庢劗绱掔€ｎ偄鐏遍柣蹇擃儏閳规垶骞婇柛濠冩礋瀹曨垶顢涘杈ㄦ闂佸搫琚崕鏌ユ偂閺囥垺鐓冮柍杞扮閺嗙喖鏌嶉挊澶樻Ш闁?"+nameRaw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+nameNorm);
                f.type=typeNorm;
                f.name=nameNorm;
                s.fields.add(f);
            }
            list.add(s);
        }
        return list;
        /*
        while(m.find()){
            Struct s=new Struct();
            String raw=m.group(1);
            String camel=toCamel(raw);
            if(!camel.equals(raw)) addWarn("缂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤缂嶅﹪寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ょ紓宥咃躬瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁绘劦鍓欓崝銈囩磽瀹ュ拑韬€殿喖顭烽弫鎰緞婵犲嫷鍚呴梻浣瑰缁诲倿骞夊☉銏犵缂備焦顭囬崢杈ㄧ節閻㈤潧孝闁稿﹤缍婂畷鎴﹀Ψ閳哄倻鍘搁柣蹇曞仩椤曆勬叏閸屾壕鍋撳▓鍨灍闁瑰憡濞婇獮鍐ㄢ枎瀵版繂婀遍埀顒婄秵娴滄瑦绔熼弴銏♀拺闁告稑锕︾紓姘舵煕鎼淬倖鐝紒瀣槸椤撳吋寰勭€ｎ剙骞愬┑鐘灱濞夋盯鏁冮敃鈧～婵嬪Ω閳哄倻鍘搁梺閫炲苯澧紒鍌涘笧閳ь剨缍嗛崑鍡涘储閽樺鏀介柍钘夋閻忋儲绻涢崪鍐М闁轰礁绉撮濂稿幢閹邦亞鐩庨梻浣瑰缁诲倸螞濞戙垹鐭楅柍褜鍓熷娲传閸曨剚鎷辩紓浣割儐鐢偤骞戦姀鐘斀閻庯綆浜為敍婊冣攽閻樻墠鍫ュ磻閹惧墎纾兼い鏃傚亾閺嗩剚鎱ㄦ繝鍐┿仢婵☆偄鍟埥澶娾枎閹邦厼鈧兘姊虹拠鎻掝劉缁炬澘绉撮…鍨潨閳ь剟銆佸鑸垫櫜闁糕剝鐟ù鍕煟鎼搭垳鍒伴柣蹇斿哺瀵彃鈹戦崱鈺傚瘜闂侀潧鐗嗘鍛婄娴犲鐓曟慨妞诲亾缂佺姵鐗犻悰顕€骞嬮敃鈧崡鎶芥煏韫囧﹥顎嗛柟?"+raw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+camel);
            s.name=camel;
            String body=m.group(2).trim();
            for(String line: body.split(";")){
                String l=line.trim();
                if(l.isEmpty()) continue;
                String[] kv=l.split("\\s+");
                if(kv.length<2) continue;
                Field f=new Field();
                String typeRaw=kv[0].trim();
                String nameRaw=kv[1].replace(";","").trim();
                String typeNorm=normalizeTypeToken(typeRaw);
                if(!typeNorm.equals(typeRaw)) addWarn("缂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤缂嶅﹪寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ょ紓宥咃躬瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁绘劦鍓欓崝銈囩磽瀹ュ拑韬€殿喖顭烽弫鎰緞婵犲嫷鍚呴梻浣瑰缁诲倿骞夊☉銏犵缂備焦顭囬崢杈ㄧ節閻㈤潧孝闁稿﹤缍婂畷鎴﹀Ψ閳哄倻鍘搁柣蹇曞仩椤曆勬叏閸屾壕鍋撳▓鍨灍闁瑰憡濞婇獮鍐ㄢ枎瀵版繂婀遍埀顒婄秵娴滄瑦绔熼弴銏♀拺闁告稑锕︾紓姘舵煕鎼淬倖鐝紒瀣槸椤撳吋寰勭€ｎ剙骞愬┑鐘灱濞夋盯鏁冮敃鈧～婵嬪Ω閳哄倻鍘搁梺閫炲苯澧紒鍌涘笧閳ь剨缍嗛崑鍡涘储閽樺鏀介柍钘夋閻忋儲绻涢崪鍐М闁轰礁绉撮濂稿幢閹邦亞鐩庨梻浣瑰缁诲倸螞濞戙垹鐭楅柍褜鍓熷?"+s.name+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕殭闁稿﹤鎽滈弫顕€宕奸弴鐔哄幘闂佸搫顦冲▔鏇熺閵忋倖鐓冮悷娆忓閻忔挳鏌熼鐣屾噰鐎殿喖鐖奸獮瀣偐鏉堚晝顦ㄥ┑鐘殿暜缁辨洟宕戝☉銏″仱闁靛ň鏅涚粻鏍煕鐏炴儳鍤柛銈嗘礋閺岋紕浠︾拠鎻掑闂佺粯鎸婚惄顖炲蓟濞戞ǚ妲堥柛妤冨仦閻忓牓姊洪柅鐐茶嫰婢т即鏌℃担鍓茬吋鐎殿喛顕ч埥澶婎煥閸涱垱婢戦梻浣烘嚀閻忔繈宕婊呮噮濠电姷鏁搁崑娑㈡偤閵娧冨灊闁规儳澧庢稉宥夋煛瀹擃喖鏈紞搴♀攽閻愬弶鈻曞ù婊勭箞閹偞绻濆顓犲幗闂佸綊鍋婇崹浼存嫊婵傚憡鍊垫慨姗嗗幗缁跺弶銇勯鈥冲姷妞わ附濞婇弻鐔煎川婵犲倵鏋欓悗娈垮枛椤兘骞冮姀銏犳瀳閺夊牄鍔嶅▍鏃堟⒒娴ｅ憡鍟炲〒姘殜瀹曘垺绂掔€ｎ偄浠у┑鐘绘涧椤戝棝宕愰悽鍛婂仭婵炲棗绻愰顏嗙磼閳ь剟宕橀鍡欙紲濡炪倖妫侀崑鎰不婵犳碍瀵犳繝闈涱儐閻撴瑩鏌熼娑欑凡鐞氭岸姊虹粙鍖″伐闁诲繑宀搁獮鍫ュΩ閵夘喗寤洪梺绯曞墲椤ㄥ懐绮昏ぐ鎺撯拺闁告稑锕︾粻鏍ㄤ繆閻愭壆鐭欑€殿喖顭烽弫宥夊礋椤忓懎濯伴梺鑽ゅТ濞诧妇绮婇幘顔肩；闁圭偓绶為弮鍫濆窛妞ゆ挾濮峰畷璺衡攽閻樺灚鏆╁┑顔芥綑鐓ら柕鍫濇处閸忔粓鏌嶈閸撶喎顫忛搹鍦＜婵☆垰鎼～宥夋偡濠婂嫭绶查柛鐔稿閸掓帗绻濆顓熸珳婵犮垼娉涢鍌炲箯?"+typeRaw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+typeNorm);
                String nameNorm=toLowerCamel(nameRaw);
                if(!nameNorm.equals(nameRaw)) addWarn("缂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤缂嶅﹪寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ょ紓宥咃躬瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁绘劦鍓欓崝銈囩磽瀹ュ拑韬€殿喖顭烽弫鎰緞婵犲嫷鍚呴梻浣瑰缁诲倿骞夊☉銏犵缂備焦顭囬崢杈ㄧ節閻㈤潧孝闁稿﹤缍婂畷鎴﹀Ψ閳哄倻鍘搁柣蹇曞仩椤曆勬叏閸屾壕鍋撳▓鍨灍闁瑰憡濞婇獮鍐ㄢ枎瀵版繂婀遍埀顒婄秵娴滄瑦绔熼弴銏♀拺闁告稑锕︾紓姘舵煕鎼淬倖鐝紒瀣槸椤撳吋寰勭€ｎ剙骞愬┑鐘灱濞夋盯鏁冮敃鈧～婵嬪Ω閳哄倻鍘搁梺閫炲苯澧紒鍌涘笧閳ь剨缍嗛崑鍡涘储閽樺鏀介柍钘夋閻忋儲绻涢崪鍐М闁轰礁绉撮濂稿幢閹邦亞鐩庨梻浣瑰缁诲倸螞濞戙垹鐭楅柍褜鍓熷?"+s.name+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕殭闁稿﹤鎽滈弫顕€宕奸弴鐔哄幘闂佸搫顦冲▔鏇熺閵忋倖鐓冮悷娆忓閻忔挳鏌熼鐣屾噰鐎殿喖鐖奸獮瀣偐鏉堚晝顦ㄥ┑鐘殿暜缁辨洟宕戝☉銏″仱闁靛ň鏅涚粻鏍煕鐏炴儳鍤柛銈嗘礋閺岋紕浠︾拠鎻掑闂佺粯鎸婚惄顖炲蓟濞戞ǚ妲堥柛妤冨仦閻忓牓姊洪柅鐐茶嫰婢т即鏌℃担鍓茬吋鐎殿喛顕ч埥澶婎煥閸涱垱婢戦梻浣烘嚀閻忔繈宕婊呮噮濠电姷鏁搁崑娑㈡偤閵娧冨灊闁规儳澧庢稉宥夋煛瀹擃喖鏈紞搴♀攽閻愬弶鈻曞ù婊勭矊椤斿繐鈹戦崱蹇旀杸闂佺粯蓱瑜板啴寮冲▎鎰╀簻闁挎棁顫夊▍濠冩叏婵犲懏顏犵紒顔界懃閳诲酣骞嗚婢瑰牊淇婇悙顏勨偓褔姊介崟顒傜濠电姴鎳愰悢?"+nameRaw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+nameNorm);
                f.type=typeNorm; f.name=nameNorm; 
                s.fields.add(f);
            }
            list.add(s);
        }
        return list;
        */
    }
    static void applyStructAnnotations(Struct s, String rawAnnotations){
        if(rawAnnotations==null || rawAnnotations.isBlank()){
            return;
        }
        Matcher matcher=Pattern.compile("@(\\w+)").matcher(rawAnnotations);
        while(matcher.find()){
            String annotation=matcher.group(1).toLowerCase(Locale.ROOT);
            switch (annotation){
                case "hot":
                    s.hot=true;
                    break;
                case "fixed":
                    s.fixed=true;
                    break;
                case "inline":
                    s.inline=true;
                    break;
                default:
                    addWarn("unknown struct annotation @"+annotation+" ignored");
                    break;
            }
        }
    }
    static void applyFieldAnnotations(Field field, String rawAnnotations){
        if(rawAnnotations==null || rawAnnotations.isBlank()){
            return;
        }
        Matcher matcher=Pattern.compile("@(\\w+)(?:\\(([^)]*)\\))?").matcher(rawAnnotations);
        while(matcher.find()){
            String annotation=matcher.group(1).toLowerCase(Locale.ROOT);
            String argument=matcher.group(2);
            switch (annotation){
                case "packed":
                    field.packed=true;
                    break;
                case "borrow":
                    field.borrow=true;
                    break;
                case "fixed":
                    if(argument==null || argument.isBlank()){
                        addWarn("field annotation @fixed requires a numeric length");
                    }else{
                        try{
                            field.fixedLength=Integer.parseInt(argument.trim());
                        }catch (NumberFormatException ex){
                            addWarn("invalid @fixed length: "+argument);
                        }
                    }
                    break;
                default:
                    addWarn("unknown field annotation @"+annotation+" ignored");
                    break;
            }
        }
    }
    static final List<String> WARNINGS = new ArrayList<>();
    static void addWarn(String s){ WARNINGS.add(s); }
    static String toCamel(String name){
        if(name==null||name.isEmpty()) return name;
        if(!hasWordSeparators(name)) return Character.toUpperCase(name.charAt(0))+name.substring(1);
        String[] parts=name.split("[^A-Za-z0-9]+");
        StringBuilder sb=new StringBuilder();
        for(String p: parts){
            if(p.isEmpty()) continue;
            sb.append(normalizePascalSegment(p));
        }
        String r=sb.toString();
        if(r.isEmpty()) return name.substring(0,1).toUpperCase()+name.substring(1);
        return r;
    }
    static String toLowerCamel(String name){
        if(name==null||name.isEmpty()) return name;
        if(!hasWordSeparators(name)) return Character.toLowerCase(name.charAt(0))+name.substring(1);
        String[] parts=name.split("[^A-Za-z0-9]+");
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<parts.length;i++){
            String p=parts[i];
            if(p.isEmpty()) continue;
            String normalized=normalizePascalSegment(p);
            if(i==0) sb.append(Character.toLowerCase(normalized.charAt(0))).append(normalized.substring(1));
            else sb.append(normalized);
        }
        return sb.toString();
    }
    static String normalizeTypeToken(String t){
        String s=trimGenericTokenSpaces(t);
        if(s.endsWith("[]")){
            String base=s.substring(0,s.length()-2);
            return Codegen.mapType(base)+"[]";
        }
        if(Codegen.isContainerType(s)){
            return Codegen.toGenericWithWrappers(s);
        }
        if(isPrimitiveToken(s) || s.equals("String")) return s;
        return toCamel(s);
    }
    static String[] splitTypeAndName(String declaration){
        int level=0;
        for(int i=declaration.length()-1;i>=0;i--){
            char c=declaration.charAt(i);
            if(c=='>') level++;
            else if(c=='<') level--;
            else if(Character.isWhitespace(c) && level==0){
                int left=i;
                while(left>=0 && Character.isWhitespace(declaration.charAt(left))) left--;
                int right=i+1;
                while(right<declaration.length() && Character.isWhitespace(declaration.charAt(right))) right++;
                if(left>=0 && right<declaration.length()){
                    return new String[]{declaration.substring(0, left+1), declaration.substring(right)};
                }
            }
        }
        return null;
    }
    static boolean hasWordSeparators(String name){
        for(int i=0;i<name.length();i++){
            if(!Character.isLetterOrDigit(name.charAt(i))) return true;
        }
        return false;
    }
    static String normalizePascalSegment(String segment){
        if(segment.isEmpty()) return segment;
        if(segment.length()==1) return segment.toUpperCase(Locale.ROOT);
        return Character.toUpperCase(segment.charAt(0))+segment.substring(1).toLowerCase(Locale.ROOT);
    }
    static String trimGenericTokenSpaces(String token){
        return token.trim().replaceAll("\\s+", "");
    }
    static boolean isPrimitiveToken(String t){
        return "int".equals(t)||"long".equals(t)||"byte".equals(t)||"short".equals(t)
                ||"boolean".equals(t)||"char".equals(t)||"float".equals(t)||"double".equals(t);
    }
    static Proto parseProto(String text){
        Proto proto=new Proto();
        Pattern bc=Pattern.compile("client_to_server\\s*:\\s*([\\s\\S]*?)(server_to_client\\s*:|\\z)", Pattern.DOTALL);
        Matcher mc=bc.matcher(text);
        if(mc.find()){
            String body=mc.group(1);
            proto.c2s.addAll(parseMethods(body));
        }
        Pattern bs=Pattern.compile("server_to_client\\s*:\\s*([\\s\\S]*)", Pattern.DOTALL);
        Matcher ms=bs.matcher(text);
        if(ms.find()){
            String body=ms.group(1);
            proto.s2c.addAll(parseMethods(body));
        }
        if(proto.c2s.isEmpty() && proto.s2c.isEmpty()) return null;
        return proto;
    }
    static List<Method> parseMethods(String body){
        List<Method> list=new ArrayList<>();
        Pattern pm=Pattern.compile("(\\w+)\\s*\\(([^)]*)\\)\\s*;");
        Matcher mm=pm.matcher(body);
        while(mm.find()){
            Method me=new Method();
            me.name=mm.group(1);
            String params=mm.group(2).trim();
            if(!params.isEmpty()){
                for(String paramDecl: Codegen.splitTopLevel(params, ',')){
                    String param=paramDecl.trim();
                    if(param.isEmpty()) continue;
                    String[] typeAndName=splitTypeAndName(param);
                    if(typeAndName==null) continue;
                    Field f=new Field();
                    String typeRaw=typeAndName[0].trim();
                    String nameRaw=typeAndName[1].trim();
                    String typeNorm=normalizeTypeToken(typeRaw);
                    if(!typeNorm.equals(typeRaw)) addWarn("闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；闁绘劗鍎ら崑瀣煟濡崵婀介柍褜鍏涚欢姘嚕閹绢喖顫呴柍鈺佸暞閻濇牠姊绘笟鈧埀顒傚仜閼活垱鏅堕弶娆剧唵閻熸瑥瀚粈瀣煙椤旀儳鍘存鐐诧攻缁绘繈宕掑鍛呫劑姊虹拠鈥虫灀闁哄懏鐩、娆掔疀濞戣鲸鏅╅梺缁樻尭妤犳瓕鐏囩紓鍌氬€搁崐鎼佸磹閹间礁纾归柛婵勫劤閻捇鏌ｉ姀鐘冲暈闁稿顑夐弻锟犲炊閿濆棗娅氶梺閫炲苯澧惧┑鈥虫喘楠炴垿宕熼姣尖晠鏌ㄩ弴妤€浜剧紒鍓ц檸閸ㄨ泛顫忛搹鍦＜婵☆垵娅ｆ禒鎼佹煢閸愵喕鎲鹃柡宀€鍠栭幃婊堝箣閹烘挸鏋ゆ繝?"+me.name+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤缂嶅﹪寮婚敐澶婄闁挎繂鎲涢幘缁樼厱濠电姴鍊归崑銉╂煛鐏炶濮傜€殿喗鎸抽幃娆徝圭€ｎ亙澹曢悷婊呭鐢帞澹曢崸妤佺厵閻庣數顭堟牎闂佸摜濮甸崝娆撳蓟閿濆憘鏃堝焵椤掑嫭鍋嬮柛鈩冪懅缁犳棃鏌熼悜妯烩拻缁炬儳銈搁弻宥堫檨闁告挻鑹鹃銉╁礋椤撴繃鍕冪紓浣割儏閵囨ɑ绔熼弴銏♀拺缂佸娉曠粻鐗堛亜閿旇鐏＄紒鍌氱У閵堬綁宕橀埞鐐闂傚倷绶￠崑鍡涘磻濞戙垺鍤愭い鏍ㄧ⊕濞呯娀鏌熺紒銏犳灍闁绘挻娲熼弻宥囨喆閸曨偄濮㈡繛瀛樼矌閸嬫挻绌辨繝鍥ㄥ€锋い蹇撳閸嬫捇寮介锝嗘婵犵數濮寸€氼噣鎯岄崱妞尖偓鎺戭潩閿濆懍澹曢柣搴ゎ潐濞叉粓寮繝姘モ偓浣肝旈崨顓狀槹濡炪倖甯掗崐鎼佺嵁閸儲鈷掑ù锝囩摂濞兼劙鏌涙惔銏犫枙妞ゃ垺宀搁、姗€濮€閻樼數鏋冮梻浣规偠閸庮垶宕濇繝鍐洸婵犲﹤鐗婇悡蹇涚叓閸ヮ灒鍫ュ磻閹捐绀冮柛娆忣槹閸ゅ牓姊婚崒娆愮グ婵℃ぜ鍔戦幃鐐烘晝閸屾氨顦梺鍝勮癁鐏炶姤顔傞梻浣告啞濞诧箓宕愰悩缁樺亜闁惧繐婀遍敍婊冣攽閳藉棗鐏ョ€规洑绲婚妵鎰吋婢跺鎷婚梺绋挎湰閼归箖鍩€椤掑嫷妫戠紒顔肩墛缁楃喖鍩€椤掑嫮宓佸鑸靛姇閻忔娊鎮洪幒宥囧妽婵＄偘绮欏顐﹀礃椤旇偐锛滃┑顔缴戦崬鑽ゆ闁秵鈷掑ù锝呮啞閸熺偟绱掔€ｎ偄鐏撮柟顔芥そ婵℃悂鍩℃担鐚寸串闂備礁澹婇悡鍫ュ磻娴ｅ湱顩叉繝濠傜墛閻撴瑩鏌ｉ幋鐏活亪鎮橀妷锔轰簻闊浄绲奸柇顖炴煛鐏炵晫效闁哄被鍔庨埀顒婄秵娴滅偞瀵煎畝鍕拺閻犲洠鈧櫕鐏堥梺鎼炲灪閻擄繝宕洪姀鈩冨劅闁靛牆娲ㄩ弶鎼佹⒑閻熸壆浠㈤柛鐔叉櫇濡叉劕鈹戠€ｎ偀鎷绘繛杈剧到閹诧繝骞夐幖浣圭厱闁绘ê寮堕幑锝夋煙楠炲灝鐏╂い顐ｇ矒閸┾偓妞ゆ巻鍋撴い鏇悼閹风姴霉鐎ｎ偒娼旈梻渚€娼х换鍡涘焵椤掆偓閸樻牕效濡ゅ懏鐓熼幖娣€ゅ鎰箾閸欏澧悡銈夋煥閺囩偛鈧摜绮堥崱妯肩闁瑰瓨鐟ラ悘顏堟煃闁垮鐏撮柡宀€鍠撶划娆撳箰鎼淬垹闂紓鍌欒兌婵敻鏁冮姀銈呰摕婵炴垶鍩冮崑鎾绘晲閸愩劌顬堥梺閫炲苯澧紒璇插€块、姘舵晲婢舵ɑ鏅濋梺鎸庢磵閸嬫挾绱掗埀顒勫礃閳瑰じ绨婚梺鍝勫暙濞层倖绂嶈ぐ鎺撶叆婵炴垶鐟ч惌濠囨煃鐟欏嫬鐏撮柟顔界懇瀵爼骞嬮悩杈敇闂傚倷鑳堕崢褏绱炴繝鍕笉闁哄稁鍘介崑?"+typeRaw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+typeNorm);
                    String nameNorm=toLowerCamel(nameRaw);
                    if(!nameNorm.equals(nameRaw)) addWarn("闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；闁绘劗鍎ら崑瀣煟濡崵婀介柍褜鍏涚欢姘嚕閹绢喖顫呴柍鈺佸暞閻濇牠姊绘笟鈧埀顒傚仜閼活垱鏅堕弶娆剧唵閻熸瑥瀚粈瀣煙椤旀儳鍘存鐐诧攻缁绘繈宕掑鍛呫劑姊虹拠鈥虫灀闁哄懏鐩、娆掔疀濞戣鲸鏅╅梺缁樻尭妤犳瓕鐏囩紓鍌氬€搁崐鎼佸磹閹间礁纾归柛婵勫劤閻捇鏌ｉ姀鐘冲暈闁稿顑夐弻锟犲炊閿濆棗娅氶梺閫炲苯澧惧┑鈥虫喘楠炴垿宕熼姣尖晠鏌ㄩ弴妤€浜剧紒鍓ц檸閸ㄨ泛顫忛搹鍦＜婵☆垵娅ｆ禒鎼佹煢閸愵喕鎲鹃柡宀€鍠栭幃婊堝箣閹烘挸鏋ゆ繝?"+me.name+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤缂嶅﹪寮婚敐澶婄闁挎繂鎲涢幘缁樼厱濠电姴鍊归崑銉╂煛鐏炶濮傜€殿喗鎸抽幃娆徝圭€ｎ亙澹曢悷婊呭鐢帞澹曢崸妤佺厵閻庣數顭堟牎闂佸摜濮甸崝娆撳蓟閿濆憘鏃堝焵椤掑嫭鍋嬮柛鈩冪懅缁犳棃鏌熼悜妯烩拻缁炬儳銈搁弻宥堫檨闁告挻鑹鹃銉╁礋椤撴繃鍕冪紓浣割儏閵囨ɑ绔熼弴銏♀拺缂佸娉曠粻鐗堛亜閿旇鐏＄紒鍌氱У閵堬綁宕橀埞鐐闂傚倷绶￠崑鍡涘磻濞戙垺鍤愭い鏍ㄧ⊕濞呯娀鏌熺紒銏犳灍闁绘挻娲熼弻宥囨喆閸曨偄濮㈡繛瀛樼矌閸嬫挻绌辨繝鍥ㄥ€锋い蹇撳閸嬫捇寮介锝嗘婵犵數濮寸€氼噣鎯岄崱妞尖偓鎺戭潩閿濆懍澹曢柣搴ゎ潐濞叉粓寮繝姘モ偓浣肝旈崨顓狀槹濡炪倖甯掗崐鎼佺嵁閸儲鈷掑ù锝囩摂濞兼劙鏌涙惔銏犫枙妞ゃ垺宀搁、姗€濮€閻樼數鏋冮梻浣规偠閸庮垶宕濇繝鍐洸婵犲﹤鐗婇悡蹇涚叓閸ヮ灒鍫ュ磻閹捐绀冮柛娆忣槹閸ゅ牓姊婚崒娆愮グ婵℃ぜ鍔戦幃鐐烘晝閸屾氨顦梺鍝勮癁鐏炶姤顔傞梻浣告啞濞诧箓宕愰悩缁樺亜闁惧繐婀遍敍婊冣攽閳藉棗鐏ョ€规洑绲婚妵鎰吋婢跺鎷婚梺绋挎湰閼归箖鍩€椤掑嫷妫戠紒顔肩墛缁楃喖鍩€椤掑嫮宓佸鑸靛姇閻忔娊鎮洪幒宥囧妽婵＄偘绮欏顐﹀礃椤旇偐锛滃┑顔缴戦崬鑽ゆ闁秵鈷掑ù锝呮啞閸熺偟绱掔€ｎ偄鐏撮柟顔芥そ婵℃悂鍩℃担鐚寸串闂備礁澹婇悡鍫ュ磻娴ｅ湱顩叉繝濠傜墛閻撴瑩鏌ｉ幋鐏活亪鎮橀妷锔轰簻闊浄绲奸柇顖炴煛鐏炵晫效闁哄被鍔庨埀顒婄秵娴滅偞瀵煎畝鍕拺闁告繂瀚﹢鎵磼鐎ｎ偄鐏撮柛鈺冨仱楠炲鏁冮埀顒勭嵁閵忊€茬箚闁靛牆鎷戝妤冪磼鏉堛劎绠栫紒缁樼箘閸犲﹤螣閸濆嫧鎷ら柣鐔哥矋濠㈡﹢宕幍顔筋潟闁绘劕顕弧鈧梺鎼炲劀閸涱垱姣囧┑鐘茬棄閺夊簱鍋撻弴銏犵柈闁规鍠氭稉宥夋煛閸愶絽浜剧紓浣虹帛閻╊垶鐛€ｎ喗鍊烽柛鈩兠悘瀛橆殽閻愬澧垫鐐叉喘椤㈡瑩鎮欓濠勭?"+nameRaw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+nameNorm);
                    f.type=typeNorm;
                    f.name=nameNorm;
                    me.params.add(f);
                }
            }
            list.add(me);
        }
        return list;
        /*
        while(mm.find()){
            Method me=new Method(); me.name=mm.group(1);
            String params=mm.group(2).trim();
            if(!params.isEmpty()){
                String[] parts=params.split(",");
                for(String p: parts){
                    String[] kv=p.trim().split("\\s+");
                    if(kv.length>=2){
                        Field f=new Field();
                        String typeRaw=kv[0].trim();
                        String nameRaw=kv[1].trim();
                        String typeNorm=normalizeTypeToken(typeRaw);
                        if(!typeNorm.equals(typeRaw)) addWarn("闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；闁绘劗鍎ら崑瀣煟濡崵婀介柍褜鍏涚欢姘嚕閹绢喖顫呴柍鈺佸暞閻濇牠姊绘笟鈧埀顒傚仜閼活垱鏅堕弶娆剧唵閻熸瑥瀚粈瀣煙椤旀儳鍘存鐐诧攻缁绘繈宕掑鍛呫劑姊虹拠鈥虫灀闁哄懏鐩、娆掔疀濞戣鲸鏅╅梺缁樻尭妤犳瓕鐏囩紓鍌氬€搁崐鎼佸磹閹间礁纾归柛婵勫劤閻捇鏌ｉ姀鐘冲暈闁稿顑夐弻锟犲炊閿濆棗娅氶梺閫炲苯澧惧┑鈥虫喘楠炴垿宕熼姣尖晠鏌ㄩ弴妤€浜剧紒鍓ц檸閸ㄨ泛顫忛搹鍦＜婵☆垵娅ｆ禒鎼佹煢閸愵喕鎲鹃柡宀€鍠栭幃婊堝箣閹烘挸鏋ゆ繝?"+me.name+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；婵炴垟鎳為崶顒佸仺缂佸瀵ч悗顒勬⒑閻熸澘鈷旂紒顕呭灦瀹曟垿骞囬悧鍫㈠幈闂佸綊鍋婇崹鎵閿曞倹鐓熼柕蹇曞閻撳吋鎱ㄦ繝鍕笡缂佹鍠栭崺鈧い鎺嗗亾妞ゎ厼娲╅ˇ褰掓寠濠靛洢浜滈柟鏉垮閻ｉ亶鏌ｉ妶鍥т壕缂佺粯鐩獮瀣倷鐠轰警妫熸俊鐐€戦崕鎻掔暆閹间礁钃熸繛鎴欏灪閺呮粓鎮归崶銊ョ祷缂佺姵妞藉娲传閵夈儛锝夋煕閺冣偓閻熲晛顕ｆ繝姘櫜闁告稑鍊瑰Λ鍐春閳ь剚銇勯幒鎴濐仾闁稿顑夐弻娑㈠焺閸愵亝鍣紓浣哄У濡啴寮婚悢鍏煎€绘俊顖濐嚙闂夊秹姊洪崨濠勬噧缂佺粯锕㈤獮鍐ㄧ暋閹佃櫕鐎婚棅顐㈡处閹尖晜瀵奸埀顒勬⒒娴ｅ憡鍟為柣鐔村灲瀹曟垿骞樼紒妯锋嫽婵炶揪绲介幉锟犲箚閸儲鐓熸い鎾跺剱濡茶櫣绱掓潏鈺佷沪闁瑰嘲鎳樺畷婊堝箵閹烘搩鍚欏┑锛勫亼閸婃牕煤瀹ュ纾婚柟鎯х亪閸嬫挾鎲撮崟顒傤槬闂佺粯鐗曢妶鎼佸垂妤ｅ啯鏅濋柛灞炬皑椤撳搫鈹戦悩璇у伐闁哥噥鍨堕獮澶愬閵堝棌鎷婚梺绋挎湰閻燂妇绮婇悧鍫涗簻闁哄洤妫楅幊蹇撶暦閺屻儲鐓欓梺顓ㄧ畱閺嬬喓鈧娲橀悡锟犲蓟閻斿吋鍊绘慨妤€妫欓悾鍫曟⒑閸濆嫷鍎庣紒鈧笟鈧崺鐐哄箣閿旇棄浜归柣搴℃贡婵挳藟濠靛牏纾藉ù锝呮惈鏍￠梺缁橆殕缁捇鐛径鎰濞达絿鎳撴禍閬嶆⒑閸撴彃浜濈紒璇插閹兘濡搁埡鍌楁嫼缂傚倷鐒﹂敋濠殿喖娲弻銊╁即濡搫濮︽繝纰夌磿閺佽鐣烽悢纰辨晩闁告挆鍕帆闂傚倷绀佸﹢閬嶅磻閹捐绀堟慨姗嗗幖椤曢亶鏌嶈閸撴氨鎹㈠┑鍡忔灁闁割煈鍠楅悵顖滅磽娴ｇ懓绲绘い顓炲槻閻ｇ柉銇愰幒鎾崇檮婵犮垼鍩栬摫缂?"+typeRaw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+typeNorm);
                        String nameNorm=toLowerCamel(nameRaw);
                        if(!nameNorm.equals(nameRaw)) addWarn("闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；闁绘劗鍎ら崑瀣煟濡崵婀介柍褜鍏涚欢姘嚕閹绢喖顫呴柍鈺佸暞閻濇牠姊绘笟鈧埀顒傚仜閼活垱鏅堕弶娆剧唵閻熸瑥瀚粈瀣煙椤旀儳鍘存鐐诧攻缁绘繈宕掑鍛呫劑姊虹拠鈥虫灀闁哄懏鐩、娆掔疀濞戣鲸鏅╅梺缁樻尭妤犳瓕鐏囩紓鍌氬€搁崐鎼佸磹閹间礁纾归柛婵勫劤閻捇鏌ｉ姀鐘冲暈闁稿顑夐弻锟犲炊閿濆棗娅氶梺閫炲苯澧惧┑鈥虫喘楠炴垿宕熼姣尖晠鏌ㄩ弴妤€浜剧紒鍓ц檸閸ㄨ泛顫忛搹鍦＜婵☆垵娅ｆ禒鎼佹煢閸愵喕鎲鹃柡宀€鍠栭幃婊堝箣閹烘挸鏋ゆ繝?"+me.name+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；婵炴垟鎳為崶顒佸仺缂佸瀵ч悗顒勬⒑閻熸澘鈷旂紒顕呭灦瀹曟垿骞囬悧鍫㈠幈闂佸綊鍋婇崹鎵閿曞倹鐓熼柕蹇曞閻撳吋鎱ㄦ繝鍕笡缂佹鍠栭崺鈧い鎺嗗亾妞ゎ厼娲╅ˇ褰掓寠濠靛洢浜滈柟鏉垮閻ｉ亶鏌ｉ妶鍥т壕缂佺粯鐩獮瀣倷鐠轰警妫熸俊鐐€戦崕鎻掔暆閹间礁钃熸繛鎴欏灪閺呮粓鎮归崶銊ョ祷缂佺姵妞藉娲传閵夈儛锝夋煕閺冣偓閻熲晛顕ｆ繝姘櫜闁告稑鍊瑰Λ鍐春閳ь剚銇勯幒鎴濐仾闁稿顑夐弻娑㈠焺閸愵亝鍣紓浣哄У濡啴寮婚悢鍏煎€绘俊顖濐嚙闂夊秹姊洪崨濠勬噧缂佺粯锕㈤獮鍐ㄧ暋閹佃櫕鐎婚棅顐㈡处閹尖晜瀵奸埀顒勬⒒娴ｅ憡鍟為柣鐔村灲瀹曟垿骞樼紒妯锋嫽婵炶揪绲介幉锟犲箚閸儲鐓熸い鎾跺剱濡茶櫣绱掓潏鈺佷沪闁瑰嘲鎳樺畷婊堝箵閹烘搩鍚欏┑锛勫亼閸婃牕煤瀹ュ纾婚柟鎯х亪閸嬫挾鎲撮崟顒傤槰闁汇埄鍨辩敮锟犳晲閻愭祴鏀介悗锝呯仛閺呯偤鏌ｉ悩鍏呰埅闁告柨鐬肩划顓㈡晸閻樻枼鎷?"+nameRaw+" 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥闂佹寧绻勯崑娑㈠煘閹寸姭鍋撻敐搴′簼婵炲懎娲铏圭矙鐠恒劎鍔规繝纰樷偓铏窛缂侇喗鐟ㄧ粻娑㈠籍閸屾粎妲囬梻渚€娼ф蹇曞緤娴犲鍋傞柟鎵閻撴洟鏌￠崘锝呬壕闂佺粯顨堟慨鎾偩閻戣棄绠ｉ柨鏇楀亾閸ュ瓨绻濋姀锝嗙【妞ゆ垵娲畷銏ゅ箹娴ｅ厜鎷洪梺纭呭亹閸嬫盯宕濆Δ鍛厸闁告侗鍠氶埥澶愭煟椤垵澧存慨濠勭帛閹峰懘鎼归悷鎵偧闂佹眹鍩勯崹杈╂暜閿熺姴鏋侀柛鎰靛枛鍞梺瀹犳〃缁插ジ鏁冮崒娑氬幈闂佸搫娲㈤崝宀勫几閵堝鐓熼柕鍫濆€告禍楣冩⒒閸屾瑦绁版い顐㈩槸閻ｅ嘲螣鐞涒剝鐏冨┑鐐村灟閸ㄥ綊鎮￠弴鐐╂斀闁绘ɑ褰冮顐ょ棯閸欍儳鐭欓柡灞剧〒娴狅箓鎮欓鍌涱吇闂佸搫绋勭换婵嗩潖閾忓湱纾兼慨妤€妫欓悾宄扳攽閻愯泛鐨洪柛鐘崇墵瀹曡銈ｉ崘鈺傛珖闂佺鏈畝鎼佸极濠婂啠鏀介幒鎶藉磹閹惧墎鐭嗗ù锝囩《閺嬫梹绻濋棃娑卞剱闁抽攱甯￠弻娑氫沪閻愵剛娈ゆ繝鈷€鍕€掔紒杈ㄥ笧閳ь剨缍嗛崑鍕倶閹绢喗鐓ユ繝闈涚墕娴犳粍銇勯幘鍐叉倯鐎垫澘瀚埀顒婃€ラ崟顐紪闂傚倸鍊烽懗鍫曘€佹繝鍥х；闁圭増婢樼壕缁樼箾閹存瑥鐏╅柣鎺戠仛閵囧嫰骞掑鍫濆帯婵犫拃鍛毄闁逞屽墲椤煤閺嶎偆绀婂┑鐘插€婚弳锔剧磼鐎ｎ収鍤﹂柡鍐ㄧ墕閻掑灚銇勯幒鎴濐仾閻庢艾鎳橀弻锝夊棘閹稿孩鍠愮紓浣哄█缁犳牠寮婚悢琛″亾濞戞瑯鐒介柟鍐插暣閺岋綀绠涙繝鍐╃彇缂備浇椴哥敮锟犲箖閳轰胶鏆﹂柛銉ｅ妼閸ㄩ亶姊绘担鍛婃儓闁兼椿鍨崇划鏃堟濞戣京鍔峰銈呯箰閻楀棛绮婚妷锔轰簻闁哄洨鍋為崳铏规偖閿曗偓閳规垿鏁嶉崟顐℃澀闂佺顭堥崐婵嗙暦濠婂啠鏋庨柟鐐綑娴滈亶姊虹化鏇炲⒉缂佸鐗撻崺鈧い鎺嶇劍椤ュ牏鈧娲橀敃銏ゃ€佸▎鎾冲簥濠㈣鍨板ú锕傛偂閺囥垺鐓冮柍杞扮閺嬨倝鏌ｉ幒妤冪暫闁哄本绋撻埀顒婄岛閺呮繄绮ｉ弮鈧幈銊︾節閸愨斂浠㈤悗瑙勬磸閸斿秶鎹㈠┑瀣＜婵絽銇橀懗鍓佹閹惧瓨濯撮柛锔诲幖瀵劎绱撴担鍝勑ｉ柣妤冨█瀵?"+nameNorm);
                        f.type=typeNorm; f.name=nameNorm;
                        me.params.add(f);
                    }
                }
            }
            list.add(me);
        }
        return list;
        */
    }
    static class Codegen {
        private static java.util.Set<String> ENUMS=java.util.Collections.emptySet();
        private static java.util.Map<String, Struct> STRUCTS=java.util.Collections.emptyMap();
        private static boolean SIMD_ENABLED=false;  // SIMD闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；婵炴垟鎳為崶顒佸仺缂佸瀵ч悗顒勬倵楠炲灝鍔氭い锔诲灣缁牏鈧綆鍋佹禍婊堟煙閺夊灝顣抽柟顔笺偢閺屽秷顧侀柛鎾寸缁绘稒绻濋崶褏鐣哄┑掳鍊曢幊鎰暤娓氣偓閺屾盯鈥﹂幋婵囩亪婵犳鍠栨鎼佲€旈崘顔嘉ч煫鍥ㄦ尵濡诧綁姊洪幖鐐插婵炲鐩幃楣冩偪椤栨ü姹楅梺鍦劋閸ㄥ綊鏁嶅鍫熲拺缂備焦锚婵洦銇勯弴銊ュ籍鐎规洏鍨介弻鍡楊吋閸℃ぞ鐢绘繝鐢靛Т閿曘倝宕幘顔肩煑闁告洦鍨遍悡蹇涙煕閳╁喚娈旈柡鍡悼閳ь剝顫夊ú蹇涘礉鎼淬劌鐒垫い鎺嶈兌閳洟鎳ｉ妶澶嬬厵闁汇値鍨奸崵娆愩亜椤忓嫬鏆ｅ┑鈥崇埣瀹曞崬鈻庤箛锝嗘缂傚倸鍊风粈渚€顢栭崱娑樼闁告挆鍐ㄧ亰婵犵數濮甸懝鍓х矆閸垺鍠愬鑸靛姇绾惧鏌熼崜褏甯涢柛瀣剁節閺屸剝寰勭€ｉ潧鍔屽┑鈽嗗亜閻倸顫忓ú顏勪紶闁靛鍎涢敐鍡欑闁告瑥顦遍惌鎺楁煙瀹曞洤浠遍柡灞芥椤撳ジ宕卞Δ渚囧悑闂傚倷绶氬褔鎮ч崱妞曟椽濡搁埡鍌涙珫濠电姴锕ら悧濠囧煕閹达附鈷戞い鎰╁€曟禒婊堟煠濞茶鐏￠柡鍛埣椤㈡岸鍩€椤掑嫬钃熼柨婵嗩槹閺呮煡鏌涢埄鍐噮闁汇倕瀚伴幃妤冩喆閸曨剛顦梺鍝ュУ閻楃娀濡存担鑲濇棃宕ㄩ鐙呯床婵犳鍠楅敃鈺呭礈濞戙埄鏁婇柛銉墯閳锋帒霉閿濆洨鎽傞柛銈嗙懄閹便劌顫滈崼銏㈡殼闂佹寧绻勯崑鐐差嚗閸曨垰绠涙い鎺戝亞閸熷洭姊绘担绋挎毐闁圭⒈鍋婇獮濠冩償閿濆洨骞撳┑掳鍊曢幊蹇涙偂濞戞埃鍋撻獮鍨姎濡ょ姵鎮傞悰顕€寮介銈囷紲闂佺粯锕㈠褔鍩㈤崼銉︾厸鐎光偓閳ь剟宕伴弽顓犲祦鐎广儱顦介弫濠勭棯閹峰矂鍝烘慨锝咁樀濮婄粯鎷呴崨濠冨創闂佺懓鍢查澶婄暦濠婂喚娼╅弶鍫涘妼鎼村﹤鈹戦悙鏉戠仧闁搞劌婀辩划濠氭晲閸℃瑧顔曢梺绯曞墲椤ㄥ牏绮婚崘瑁佸綊鎮╅懡銈囨毇濠?
        static void setEnums(java.util.Set<String> s){ ENUMS=s; }
        static void setStructs(java.util.Collection<Struct> structs){
            java.util.Map<String, Struct> map=new java.util.LinkedHashMap<>();
            for(Struct struct: structs){
                map.put(struct.name, struct);
            }
            STRUCTS=map;
        }
        static void setSimdEnabled(boolean enabled){ SIMD_ENABLED=enabled; }
        static boolean isSimdEnabled(){ return SIMD_ENABLED; }
        static String generateEnum(String pkg, EnumDef e){
            // 濠电姷鏁告慨鐑藉极閸涘﹥鍙忛柣鎴ｆ閺嬩線鏌涘☉姗堟敾闁告瑥绻橀弻锝夊箣濠垫劖缍楅梺閫炲苯澧柛濠傛健楠炴劖绻濋崘顏嗗骄闂佸啿鎼鍥╃矓椤旈敮鍋撶憴鍕８闁告梹鍨甸锝夊醇閺囩偟顓洪梺缁樼懃閹虫劙鐛姀銈嗏拻闁稿本鐟чˇ锕傛煙濞村澧茬紒妤冨枎铻栭柛娑卞幘閻撴垿鏌熼崗鑲╂殬闁告柨绉瑰畷鎴﹀礋椤栨稓鍘遍梺鏂ユ櫅閸橀箖鎳栭埡鍌氬簥闂佺硶鍓濊彠濞存粍绮撻弻鈥愁吋閸愩劌顬夐梺姹囧妽閸ㄥ爼骞堥妸鈺傛櫜闁搞儜鍌涱潟闂備礁鎼張顒傜矙閹捐鐒垫い鎺戯功缁夌敻鏌涚€ｎ亝鍣藉ù婊勬倐椤㈡﹢鎮㈢紙鐘电泿婵＄偑鍊栭崝褏寰婄捄銊т笉闁绘劗鍎ら悡娆愩亜閺冨倹鍤€濠⒀勭叀閺岀喖顢涘☉娆樻闂佺硶鏅粻鎾诲春閳ь剚銇勯幒鎴濐仼缂佺媭鍨遍妵鍕箛閸洘顎嶉梺缁樻尵閸犳牠鐛弽顬ュ酣顢楅埀顒勫焵椤戞儳鈧洟鈥﹂崶顒€绠涙い鎾跺Х椤旀洟姊洪崨濠勬噧妞わ箒浜划濠氭倷閻戞鍙嗗┑鐘绘涧閻楀棙绂掗敂閿亾閸偅绶查悗姘嵆閻涱噣宕堕澶嬫櫌闂佺鏈划宥呅掓惔銊︹拻闁稿本鐟чˇ锕傛煙绾板崬浜扮€规洦鍨堕、鏇㈡晜閽樺缃曢梻浣虹《閸撴繈鏁嬮梺鍛婃⒐濡啫顫忔繝姘＜婵炲棙鍨垫俊浠嬫煟鎼达絿鎳楅柛鎰亾缂嶅酣鎮峰鍛暭閻㈩垱甯炴竟鏇犳崉閵娿垹浜鹃悷娆忓缁€鈧┑鐐额嚋缁犳挸顕ｉ崘宸叆闁割偅绻勯鎰攽閻戝洨绉甸柛鎾寸懄娣囧﹥绂掔€ｎ偆鍘介梺瑙勫礃濞夋盯寮稿☉娆樻闁绘劕顕晶顒佺箾閻撳海绠荤€规洘绮忛ˇ鎾煥濞戞艾鏋涙慨濠勫劋鐎电厧鈻庨幋鐘橈綁姊洪崨濠勬噧闁哥喐娼欓锝囨嫚濞村顫嶅┑鐐叉閸旀洟宕濋崨瀛樷拺闂傚牊渚楅悞楣冩煕婵犲啰澧电€规洘婢橀～婵嬵敄閳哄倹顥堥柟顔规櫊濡啫鈽夊Δ鍐╁礋缂傚倸鍊烽懗鍓佸垝椤栨粍鏆滈柨鐔哄Т閺勩儵鏌嶈閸撴岸濡甸崟顖氱闁规惌鍨版慨娑氱磽娴ｅ壊妲洪柡浣割煼瀵鈽夐姀鈥充汗閻庤娲栧ú銈夊煕瀹€鍕拺閻犲洠鈧櫕鐏堝┑鐐点€嬬换婵嬪Υ娴ｅ壊娼╅悹楦挎閸旓箑顪冮妶鍡楃瑨閻庢凹鍓熼幏鎴︽偄閸濄儳顔曢梺鐟扮摠閻熴儵鎮橀埡鍛埞妞ゆ牗鍑瑰〒濠氭煏閸繃顥為柍閿嬪浮閺屾稑螣閻樺弶绁紓宥嗙墬閵囧嫯绠涢幘璺侯杸闂佹娊鏀遍崹鍧楀蓟閻旂厧绠氶柡澶婃櫇閹剧粯鐓涘〒姘ｅ亾濞存粌鐖煎璇测槈閵忕姈鈺呮煏婢舵稓鐣卞ù鐘虫尦閹鈻撻崹顔界亪濡炪値鍘鹃崗姗€鐛崘顔碱潊闁靛牆妫欓崕顏堟⒑闂堚晛鐦滈柛娆忕箳濡叉劙宕ｆ径宀€鐦堢紒鍓у钃辨い顐躬閺屾盯濡搁敃鈧埢鏇犫偓瑙勬礃濞茬喐淇婇崼鏇炵倞闁靛鍎宠ぐ鎾⒒娴ｈ櫣甯涢柛鏃€顨婂畷鏇㈠Χ婢跺﹦鍘遍梺鐟邦嚟婵澹曢挊澹濆綊鏁愰崼顐㈡異闂佺粯甯婄划娆撳蓟瀹ュ鏁嶆繛鎴炵懅椤︻厾绱撴担浠嬪摵閻㈩垽绻濋妴浣糕枎閹惧磭顦ч梺绋跨箳閸樠囨⒒椤栨稓绡€缁剧増菤閸嬫捇宕橀懠顒勭崜闂備礁鎲″褰掓偡閳哄懏鍋樻い鏇楀亾妤犵偞甯￠獮濠囨惞椤愶綆妫冮梺绯曟杹閸嬫挸顪冮妶鍡楃瑨閻庢凹鍙冨畷鏇炍旀担椋庣畾闂侀潧鐗嗙€氼參藝妞嬪海纾奸悹鍥у级椤ョ偤鏌曢崶褍顏€殿喕绮欐俊姝岊檨闁哄棴绻濆铏规嫚閳ュ磭浠╅梺缁橆殔缁绘帒危閹版澘绠抽柟鎯у閹虫繈姊洪幖鐐插妧闁告洦鍘肩紞鍡涙⒒閸屾瑦绁版い鏇熺墵瀹曟澘螖閸涱偀鍋撻崘顔奸唶闁靛鍎抽悿鍛存⒑閸︻叀妾搁柛鐘崇墱缁牏鈧綆鍋佹禍婊堟煙閻戞ê鐏ュù婊呭仦娣囧﹪鎳犻鈧。鑲╃磼缂佹绠橀柛鐘诧攻瀵板嫬鐣濋埀顒勬晬閻斿吋鈷戠紒瀣儥閸庢劖銇勯鐐村枠鐎规洘宀搁獮鎺楀箣閺冣偓閻庡姊虹憴鍕婵炲绋撶划濠囨晝閸屾稈鎷洪梺鍛婄箓鐎氼噣鍩㈡径鎰厱婵☆垱浜介崑銏☆殽閻愭潙鐏撮柟铏矒閹瑩鏌呭☉姘辨晨闂傚倷娴囬～澶婄暦濡　鏋栨繛鎴欏灩閸戠娀骞栧ǎ顒€濡介柣鎾跺枑缁绘繈妫冨☉娆忔閻庤鎸稿Λ娆撳箞閵婏妇绡€闁告劏鏂傛禒銏ゆ倵濞堝灝娅橀柛鎾跺枑娣囧﹪鎮滈懞銉︽珕闂佷紮绲介懟顖滃緤娴犲鈷掗柛灞剧懅椤︼箓鏌熼懞銉х煉鐎规洘濞婃俊鐑藉煛娴ｅ摜鈧參鏌ｉ悩鐑樸€冮悹鈧敃鍌氬惞闁哄洢鍨洪崐鐢告煕閿旇骞栭崯鎼佹⒑濮瑰洤鈧劙宕戦幘缁樷拻濞达絽鎲＄拹锟犳煣韫囨捇鍙勭€规洖缍婇弻鍡楊吋閸涱噮妫熼梻渚€鈧偛鑻晶瀛樻叏婵犲嫮甯涢柟宄版嚇閹煎綊鎮烽幍顕呭仹闂傚倷绀侀幉鈥愁潖閻熸噴娲冀椤掑倷鑸繝鐢靛Х閺佸憡鎱ㄩ弶鎳ㄦ椽濡舵径濠呅曢悷婊呭鐢鎮￠悢鍏肩厸闁稿本绻冪涵鑸电箾閸儰鎲鹃柡宀嬬節閸┾偓妞ゆ帒瀚崵宥夋煏婢舵稓瀵肩紒銊ヮ煼濮婃椽宕崟顓夌娀鏌涢弬璺ㄧ劯鐎规洜鏁婚、妤呭礋椤掑倸骞堥梻渚€娼ч悧鍡椕洪妶澶婂嚑闁哄啫鐗婇悡鍐喐濠婂牆绀堟繛鍡樻尰閸婅埖鎱ㄥ鍡楀⒒闁绘柨妫欓幈銊ヮ渻鐠囪弓澹曢梻浣芥〃缁€渚€宕幘顔衡偓渚€寮崼婵堫槹濡炪倖鎸鹃崰鎰邦敊閺囩姷纾介柛灞剧懅椤︼附銇勯幋婵堝ⅵ妞ゃ垺宀搁獮搴ㄦ寠婢跺瞼娼夐梻渚€鈧偛鑻晶瀛橆殽閻愯尙绠荤€规洏鍔庨埀顒佺⊕鑿ら柟宄扮秺濮婇缚銇愰幒鎴滃枈闂佸憡鎸婚悷銉暰?00闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕偓浠嬫晸閵夆晛纾婚柕蹇嬪€栭悡鏇㈡煟閹邦垰鐨洪柛鈺嬬稻閹便劍绻濋崘鈹夸虎濠碘槅鍋勯崯顐﹀煡婢跺缍囬柕濞垮灪閻忎線姊婚崒娆戭槮闁硅姤绮嶉幈銊╂偨閹肩偐鍋撻崘鈺冪瘈闁稿被鍊曞▓?+ 婵犵數濮烽弫鍛婃叏閻戣棄鏋侀柛娑橈攻閸欏繘鏌ｉ幋锝嗩棄闁哄绶氶弻娑樷槈濮楀牊鏁鹃梺鍛婄懃缁绘﹢寮婚敐澶婎潊闁绘ê妯婂Λ宀勬⒑鏉炴壆顦﹂柨鏇ㄤ邯瀵鍨鹃幇浣告倯闁硅偐琛ラ埀顒€纾鎰版⒒閸屾艾鈧悂宕戦崱娑樺瀭闂侇剙绉存闂佸憡娲﹂崹浼村礃閳ь剟姊洪棃娴ゆ盯宕ㄩ姘瑢缂傚倸鍊搁崐宄懊归崶鈺冪濞村吋娼欑壕瑙勭節闂堟侗鍎忛柦鍐枛閺屻劌鈹戦崱鈺傂ч梺鍝勬噺閻擄繝寮诲☉妯锋闁告鍋為悘宥夋⒑閸︻厼鍘村ù婊冪埣楠炲啫螖閸愨晛鏋傞梺鍛婃处閸撴盯藝閵娾晜鈷戠紓浣股戦幆鍫㈢磼缂佹绠為柣娑卞櫍瀹曟﹢濡告惔銏☆棃鐎规洏鍔戦、娆撴嚍閵壯冪闂傚倷鑳堕、濠囧磻閹邦喗鍋橀柕澶嗘櫅缁€鍫熺節闂堟侗鍎愰柛濠傚閳ь剙绠嶉崕閬嵥囨导鏉戠厱闁瑰濮风壕钘壝归敐鍫濅簵闁瑰濮抽悞濠冦亜閹惧崬鐏柣鎾存礀閳规垿鎮╅幓鎺嗗亾閸︻厽瀚婚柨鐔哄У閻撴瑦顨ラ悙鑼虎闁诲繆鏅犻弻宥囨喆閸曨偆浼岄悗瑙勬礀閻栧ジ宕洪敓鐘茬妞ゅ繐鎷嬪鎾绘⒒閸屾艾鈧兘鎳楅崼鏇椻偓锕傚醇閵夈儱鐝樺銈嗗笒閸婃悂宕瑰┑鍫氬亾閸忓浜鹃梺鍛婃磵閺備線宕戦幘璇茬＜婵綆鍘藉浠嬨€侀弮鍫濆窛妞ゅ繐鎳庨褰掓⒒閸屾瑧顦﹂柟璇х磿缁瑩骞嬮敂鑺ユ珖闂侀潧鐗嗛ˇ浼村磹閸洘鐓冮弶鐐村椤斿鏌￠埀顒勬嚍閵夛絼绨婚梺鍝勫暙閸婂憡绂嶆ィ鍐╃厸闁糕檧鏅涙晶顖涖亜閵婏絽鍔﹂柟顔界懇閹崇娀顢楅埀顒勫焻閸偆绡€缁剧増锚婢ф煡鏌熼鐓庘偓瑙勭┍婵犲洦鍊荤紒娑橆儐閺呪晠姊洪懞銉冾亪藝鏉堚晜顫曢柨鏇炲€归埛鎺楁煕鐏炲墽鎳嗛柛蹇撶灱閻ヮ亪顢橀悙闈涚厽閻庤娲樼划宀勫煘閹寸姭鍋撻敐搴樺亾椤撱劎鐣甸柡宀嬬秮楠炲洭顢旈崱娆嶅仭濠电姵顔栭崰妤佺箾婵犲洤绠栫憸鐗堝笒閻愬﹦鎲告惔銊ョ厺闁哄洨鍠撶粻楣冩煙閻愵剙澧柣鎾炽偢閺岋紕浠﹂悙顒傤槹閻庤娲滈崢褔鍩為幋锕€骞㈤煫鍥ㄦ尫婢规洟姊洪懡銈呮瀾濠㈢懓顑夊绋款吋婢跺鍘甸梺鍛婃寙閸涱厾顐奸梻浣虹帛閹歌煤閻旂厧钃熼柣鏃傚帶缁犳氨鎲稿鍫濆惞闁绘柨鍚嬮悡娆撴煕閹邦剙绾ч柛鐘筹耿閺岋紕浠﹂悾灞澭冣攽閿涘嫬鍘撮柡浣稿€荤划鐢碘偓锝庡亽濡啴姊虹拠鍙夊攭妞ゎ偄顦叅婵せ鍋撻柡浣稿暣婵偓闁靛牆鍟犻崑鎾存媴缁洘鐎婚梺瑙勫劤閸熻法鑺遍妷锔剧瘈闁靛骏缍嗛崵鍐煕閵婏附绶查悡銈夋煥閺囩偛鈧綊宕戦崒鐐寸厸闁搞儮鏅涢弸鏃傜磼閻樿崵鐣洪柡宀€鍠撻埀顒佺⊕椤洨绮婚幘缁樼厾缂佸娉曠粔娲煛瀹€瀣？闁逞屽墾缂嶁偓婵炲鐩妴鍛搭敆閸曨剛鍘介梺闈涱樈閸犳洟鏌囬娑栦簻闁靛繆鍩楅鍫濈叀濠㈣泛谩閻斿吋鍋￠梺顓ㄩ檮椤ワ繝姊?0闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕偓浠嬫晸閵夆晛纾婚柕蹇嬪€栭悡鏇㈡煟閹邦垰鐨洪柛鈺嬬稻閹便劍绻濋崘鈹夸虎濠碘槅鍋勯崯顐﹀煡婢跺缍囬柕濞垮灪閻忎線姊婚崒娆戭槮闁硅姤绮嶉幈銊╂偨閹肩偐鍋撻崘鈺冪瘈闁稿被鍊曞▓?
            int estimatedSize=300+e.items.size()*30;
            StringBuilder sb=new StringBuilder(estimatedSize);
            sb.append("package ").append(pkg).append(";\n\n");
            sb.append("public enum ").append(e.name).append(" {");
            for(int i=0;i<e.items.size();i++){
                sb.append(i==0? "\n    ":"\n  , ").append(e.items.get(i));
            }
            sb.append(";\n\n");
            sb.append("    // Cache values() once to avoid clone on hot path.\n");
            sb.append("    private static final ").append(e.name).append("[] VALUES = values();\n\n");
            sb.append("    public static ").append(e.name).append(" fromOrdinal(int ordinal){\n");
            sb.append("        return VALUES[ordinal];\n");
            sb.append("    }\n");
            sb.append("}\n");
            return sb.toString();
        }
        static String generateStruct(String pkg, Struct s){
            List<Field> presenceFields=presenceFields(s.fields);
            boolean fixedLayout=isFixedStruct(s);
            if(s.fixed && !fixedLayout){
                addWarn("struct "+s.name+" marked @fixed but contains variable-length fields; fallback to normal codec");
            }
            if(fixedLayout){
                return generateFixedStruct(pkg, s);
            }
            // 濠电姷鏁告慨鐑藉极閸涘﹥鍙忛柣鎴ｆ閺嬩線鏌涘☉姗堟敾闁告瑥绻橀弻锝夊箣濠垫劖缍楅梺閫炲苯澧柛濠傛健楠炴劖绻濋崘顏嗗骄闂佸啿鎼鍥╃矓椤旈敮鍋撶憴鍕８闁告梹鍨甸锝夊醇閺囩偟顓洪梺缁樼懃閹虫劙鐛姀銈嗏拻闁稿本鐟чˇ锕傛煙濞村澧茬紒妤冨枎铻栭柛娑卞幘閻撴垿鏌熼崗鑲╂殬闁告柨绉瑰畷鎴﹀礋椤栨稓鍘遍梺鏂ユ櫅閸橀箖鎳栭埡鍌氬簥闂佺硶鍓濊彠濞存粍绮撻弻鈥愁吋閸愩劌顬夐梺姹囧妽閸ㄥ爼骞堥妸鈺傛櫜闁搞儜鍌涱潟闂備礁鎼張顒傜矙閹捐鐒垫い鎺戯功缁夌敻鏌涚€ｎ亝鍣藉ù婊勬倐椤㈡﹢鎮㈢紙鐘电泿婵＄偑鍊栭崝褏寰婄捄銊т笉闁绘劗鍎ら悡娆愩亜閺冨倹鍤€濠⒀勭叀閺岀喖顢涘☉娆樻闂佺硶鏅粻鎾诲春閳ь剚銇勯幒鎴濐仼缂佺媭鍨遍妵鍕箛閸洘顎嶉梺缁樻尵閸犳牠鐛弽顬ュ酣顢楅埀顒勫焵椤戞儳鈧洟鈥﹂崶顒€绠涙い鎾跺Х椤旀洟姊洪崨濠勬噧妞わ箒浜划濠氭倷閻戞鍙嗗┑鐘绘涧閻楀棙绂掗敂閿亾閸偅绶查悗姘嵆閻涱噣宕堕澶嬫櫌闂佺鏈划宥呅掓惔銊︹拻闁稿本鐟чˇ锕傛煙绾板崬浜扮€规洦鍨堕、鏇㈡晜閽樺缃曢梻浣虹《閸撴繈鏁嬮梺鍛婃⒐濡啫顫忔繝姘＜婵炲棙鍨垫俊浠嬫煟鎼达絿鎳楅柛鎰亾缂嶅酣鎮峰鍛暭閻㈩垱甯炴竟鏇犳崉閵娿垹浜鹃悷娆忓缁€鈧┑鐐额嚋缁犳挸顕ｉ崘宸叆闁割偅绻勯鎰攽閻戝洨绉甸柛鎾寸懄娣囧﹥绂掔€ｎ偆鍘介梺瑙勫礃濞夋盯寮稿☉娆樻闁绘劕顕晶顒佺箾閻撳海绠荤€规洘绮忛ˇ鎾煥濞戞艾鏋涙慨濠勫劋鐎电厧鈻庨幋鐘橈綁姊洪崨濠勬噧闁哥喐娼欓锝囨嫚濞村顫嶅┑鐐叉閸旀洟宕濋崨瀛樷拺闂傚牊渚楅悞楣冩煕婵犲啰澧电€规洘婢橀～婵嬵敄閳哄倹顥堥柟顔规櫊濡啫鈽夊Δ鍐╁礋缂傚倸鍊烽懗鍓佸垝椤栨粍鏆滈柨鐔哄Т閺勩儵鏌嶈閸撴岸濡甸崟顖氱闁规惌鍨版慨娑氱磽娴ｅ壊妲洪柡浣割煼瀵鈽夐姀鈥充汗閻庤娲栧ú銈夊煕瀹€鍕拺閻犲洠鈧櫕鐏堝┑鐐点€嬬换婵嬪Υ娴ｅ壊娼╅悹楦挎閸旓箑顪冮妶鍡楃瑨閻庢凹鍓熼幏鎴︽偄閸濄儳顔曢梺鐟扮摠閻熴儵鎮橀埡鍛埞妞ゆ牗鍑瑰〒濠氭煏閸繃顥為柍閿嬪浮閺屾稑螣閻樺弶绁紓宥嗙墬閵囧嫯绠涢幘璺侯杸闂佹娊鏀遍崹鍧楀蓟閻旂厧绠氶柡澶婃櫇閹剧粯鐓涘〒姘ｅ亾濞存粌鐖煎璇测槈閵忕姈鈺呮煏婢舵稓鐣卞ù鐘虫尦閹鈻撻崹顔界亪濡炪値鍘鹃崗姗€鐛崘顔碱潊闁靛牆妫欓崕顏堟⒑闂堚晛鐦滈柛娆忕箳濡叉劙宕ｆ径宀€鐦堢紒鍓у钃辨い顐躬閺屾盯濡搁敃鈧埢鏇犫偓瑙勬礃濞茬喐淇婇崼鏇炵倞闁靛鍎宠ぐ鎾⒒娴ｈ櫣甯涢柛鏃€顨婂畷鏇㈠Χ婢跺﹦鍘遍梺鐟邦嚟婵澹曢挊澹濆綊鏁愰崼顐㈡異闂佺粯甯婄划娆撳蓟瀹ュ鏁嶆繛鎴炵懅椤︻厾绱撴担浠嬪摵閻㈩垽绻濋妴浣糕枎閹惧磭顦ч梺绋跨箳閸樠囨⒒椤栨稓绡€缁剧増菤閸嬫捇宕橀懠顒勭崜闂備礁鎲″褰掓偡閳哄懏鍋樻い鏇楀亾妤犵偞甯掕灃闁逞屽墰缁粯绻濆顓炩偓鐢告⒒閸喓鈯曢柟鍙夋倐閹虫牠寮介鐔叉嫽闂佺鏈悷褔藝閿曞倹鐓熼柕鍫濇噺閹兼劖銇勯埡浣靛仮濠碘剝鐡曢ˇ铏亜閹邦亞鐭欓柡灞诲姂閹垽宕崟鎴欏灪閹便劑鎮烽弶璺ㄩ獓闂侀潧娲ょ€氫即鐛鈧畷锟犳倷閸忓摜妫梻鍌欒兌缁垶骞愭ィ鍐ㄧ獥闁哄稁鍘惧畵渚€鏌″搴ｄ粓閹兼惌鐓堥弫鍡涙煃瑜滈崜姘┍婵犲洦鍤嶉柕澹拋鍟庨梻浣烘嚀閸熷潡宕查幓鎹楁椽鏁冮崒娑樹簵闂婎偄娲︾粙鎺楁偂閺囩喆浜滈柟鎵虫櫅閳ь剚娲熷鎼佸箣閿旂晫鍘辨繝鐢靛Т閸燁偅鎱ㄥ澶嬬厸鐎光偓鐎ｎ剙鍩岄梺瀹犳椤﹂潧鐣烽敓鐘冲€烽悗鐢告櫜闁垰鈹戦敍鍕杭闁稿﹥鐗犻獮鎰板箹娴ｆ瓕袝闁诲函缍嗘禍鍫曞吹濡ゅ懏鐓曢柡鍥ュ妼閳ь剛濞€閹垽鎼归崷顓ㄧ床婵犵妲呴崹闈涚暦娴ｅ啨浜归柣鏂垮悑閳锋帡鏌涚仦鎹愬闁逞屽墰閸忔﹢骞婂Δ鍛濞达絿顭堥悘濠傤渻閵堝棛澧遍柛瀣〒缁牊寰勯幇顓犲弳闂佸搫娲ㄩ崑娑㈠焵椤掆偓閹芥粎鍒掗崼銉ラ唶闁绘梻顭堝鍨攽閳藉棗鐏犻柣蹇旂箞閹繝骞囬悧鍫㈠幈闁硅偐琛ュΣ鍕叕椤掑嫭鐓涚€光偓閳ь剟宕伴弽褏鏆︽繝濠傛－濡查箖鏌ｉ姀鈺佺仭闁烩晩鍨跺璇差吋婢跺鍙嗛柣搴秵娴滅偤鎮烽妸鈺傗拻闁搞儜灞锯枅闂佸搫琚崝宀勫煘閹达箑骞㈡繛鍡楁禋閺夊憡淇婇悙顏勨偓鏇犳崲閹烘挾绠鹃柍褜鍓熼弻锛勪沪閻愵剛顦伴悗瑙勬礀瀹曨剝鐏冮柣搴秵閸樺ジ骞嗛崼銉︹拻濞达絽鎲￠幆鍫ユ煟椤撶儐妲洪柟骞垮灩閳规垿宕卞▎鎰啎闂備線娼ц墝闁哄應鏅犲顐㈩吋閸℃ê寮垮┑顔筋殔濡绂嶉悙鐑樼厵閻熸瑥瀚粈瀣叏婵犲嫮甯涢柟宄版嚇瀹曨偊濡烽敂閿亾椤旂⒈娓婚柕鍫濇閼茬娀鏌涘☉鍗炵仯闁绘挸顑夐幃宄邦煥閸涱収鏆柣銏╁灡鐢€愁嚕閸涘﹦鐟归柍褜鍓熷濠氭偄閸忚偐鍔烽梺鎸庢磵閸嬫挻顨ラ悙瀵稿⒈缂佽鲸甯″畷婊勬媴闂€鎰潟闂備礁鎼張顒勬儎椤栫偑鈧線寮撮姀鈩冩珳闂佸壊鍋呯换宥呂涢悢灏佹斀闁绘劘灏欓幗鐘电磼椤旇偐效鐎规洘娲熼獮搴ㄦ寠婢光斂鍎甸弻鐔兼倻濡櫣鍔稿┑鐐茬毞閺呯娀寮婚妸銉㈡斀闁糕剝锚濞呫垺绻濋姀锝庢綈婵炶尙鍠栧濠氭晲閸℃ê鍔呴梺闈涚墕鐎涒晝绱為崼婵冩斀闁绘劖娼欑徊鑽ょ磼缂佹◤顏堟偩瀹勬壋鏀介悗锝庝簽椤撴椽姊洪幐搴㈩梿闁靛棌鍋撻梺绋款儐閹稿骞忛崨顖氬闁哄洨鍠撻埀顒€顭峰娲偡闁箑娈舵繝娈垮枤閸忔﹢寮鍜佺叆闁割偆鍟块幏娲⒒閸屾氨澧涢柣鈺婂灦閹澘顭ㄩ崨顖滐紲闁哄鐗勯崝宥囩矆鐎ｎ亖鏀介柍銉ョ－閸╋絾銇勯姀鈩冪闁轰礁鍊婚幉鎾晲閸℃浼滈梻鍌氬€烽懗鍫曗€﹂崼銉ュ珘妞ゆ帒瀚崑锛勬喐閺傝法鏆︽繝闈涙－閸氬顭跨捄渚剰闁逞屽墰閺佸摜妲愰幒鎳虫梹鎷呯悰鈩冾潔闂備胶鎳撻崯璺ㄦ崲閹扮増鍋╅柣鎴ｆ閽冪喖鏌曟径娑橆洭闁告﹢浜堕弻锝堢疀閺囩偘鎴烽梺鎯х箰闁帮絽鐣烽悽绋跨睄闁逞屽墰閹广垹鈽夊鍡楁櫊濡炪倖妫佸畷鐢告儎鎼淬劍鐓欐い鏍ㄧ矊閺嬫稒鎱ㄦ繝鍛仩闁逞屽墮濠€杈ㄦ叏閻㈢违闁告劦浜炵壕濂告煃瑜滈崜姘辩箔閻旂厧鐒垫い鎺戝缁?000闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕偓浠嬫晸閵夆晛纾婚柕蹇嬪€栭悡鏇㈡煟閹邦垰鐨洪柛鈺嬬稻閹便劍绻濋崘鈹夸虎濠碘槅鍋勯崯顐﹀煡婢跺缍囬柕濞垮灪閻忎線姊婚崒娆戭槮闁硅姤绮嶉幈銊╂偨閹肩偐鍋撻崘鈺冪瘈闁稿被鍊曞▓?+ 婵犵數濮烽弫鍛婃叏閻戣棄鏋侀柛娑橈攻閸欏繘鏌ｉ幋锝嗩棄闁哄绶氶弻娑樷槈濮楀牊鏁鹃梺鍛婄懃缁绘﹢寮婚敐澶婎潊闁绘ê妯婂Λ宀勬⒑鏉炴壆顦﹂柨鏇ㄤ邯瀵鍨鹃幇浣告倯闁硅偐琛ラ埀顒€纾鎰版⒒閸屾艾鈧悂宕戦崱娑樺瀭闂侇剙绉存闂佸憡娲﹂崹浼村礃閳ь剟姊洪棃娴ゆ盯宕ㄩ姘瑢缂傚倸鍊搁崐宄懊归崶鈺冪濞村吋娼欑壕瑙勭節闂堟侗鍎忛柦鍐枛閺屻劌鈹戦崱鈺傂ч梺鍝勬噺閻擄繝寮诲☉妯锋闁告鍋為悘宥夋⒑閸︻厼鍘村ù婊冪埣楠炲啫螖閸愨晛鏋傞梺鍛婃处閸撴盯藝閵娾晜鈷戠紓浣股戦幆鍫㈢磼缂佹绠為柣娑卞櫍瀹曟﹢濡告惔銏☆棃鐎规洏鍔戦、娆撴嚍閵壯冪闂傚倷鑳堕、濠囧磻閹邦喗鍋橀柕澶嗘櫅缁€鍫熺節闂堟侗鍎愰柛濠傚閳ь剙绠嶉崕閬嵥囨导鏉戠厱闁瑰濮风壕钘壝归敐鍫濅簵闁瑰濮抽悞濠冦亜閹惧崬鐏柣鎾存礀閳规垿鎮╅幓鎺嗗亾閸︻厽瀚婚柨鐔哄У閻撴瑦顨ラ悙鑼虎闁诲繆鏅犻弻宥囨喆閸曨偆浼岄悗瑙勬礀閻栧ジ宕洪敓鐘茬妞ゅ繐鎷嬪鎾绘⒒閸屾艾鈧兘鎳楅崼鏇椻偓锕傚醇閵夈儱鐝樺銈嗗笒閸婂鎯屽▎鎾寸厵閻庢稒顭囨俊鍥煛鐎ｎ亞校缂佺粯绻堝Λ鍐ㄢ槈閸楃偛澹夐梻浣瑰▕閺€閬嶅垂閸︻厽顫曢柟鐑樻煛閸嬫捇鏁愭惔鈥茶埅闂佺绨洪崕鐢稿蓟濞戞瑦鍎熼柕蹇曞Т椤帡姊洪崫鍕伇闁哥姵鐗曢悾鐑藉Ω閿斿墽鐦堥梺鍛婂姂閸斿宕戦幘璇茬疀闁绘鐗忛崢閬嶆⒑瀹曞洦鍤€闁诲繑绻堝畷婵嗩煥閸涱亜浜鹃柛顭戝亝缁舵煡鎮楀鐓庡箻闁瑰箍鍨归埥澶愬閳ユ枼鍋撻柨瀣ㄤ簻闁圭儤鍨甸埀顒傛暬瀹曟垿骞橀懜闈涚彴濠电偞娼欓鍡涘棘閳ь剟姊绘担鍝ユ瀮婵☆偄瀚灋婵°倕鎳忛崐鍫曟煏婢诡垪鍋撻柛瀣尵閹叉挳宕熼鍌ゆФ闂備浇妗ㄥù鍥敋瑜旈、姘舵晲婢跺﹦顔呴梺鍏间航閸庢彃鈻嶉崱娑欌拺闁荤喐澹嗛幗鐘绘偨椤栨粌浠х紒顔款嚙椤繈鎳滅喊妯诲缂傚倸鍊烽悞锕傛晪婵犳鍣粻鎴︽箒濠电姴锕ょ€氼噣鎯岄幒妤佺厱闁宠鍎虫禍鐐繆閻愵亜鈧牜鏁繝鍥ㄥ殑闁肩鐏氬▍鐘绘煟閵忋埄鏆柛瀣尭閳绘捇宕归鐣屼邯婵＄偑鍊ら崣鍐绩鏉堛劎鈹嶅┑鐘叉搐缁犵懓霉閿濆懏鎲搁柛妯绘倐濮婅櫣绮欓幐搴㈡嫳闂佺厧婀遍崑鎾剁博閻斿憡鍎熼柍閿亾闁衡偓娴犲鐓熸俊顖濆亹鐢稒绻涢幊宄版处閻撴洖鈹戦悩鎻掓殲闁稿﹥鍔栭〃銉╂倷瀹割喗鈻堥梺杞扮劍閹瑰洭骞冮埡鍛殤妞ゆ帒锕﹂崥?00闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕偓浠嬫晸閵夆晛纾婚柕蹇嬪€栭悡鏇㈡煟閹邦垰鐨洪柛鈺嬬稻閹便劍绻濋崘鈹夸虎濠碘槅鍋勯崯顐﹀煡婢跺缍囬柕濞垮灪閻忎線姊婚崒娆戭槮闁硅姤绮嶉幈銊╂偨閹肩偐鍋撻崘鈺冪瘈闁稿被鍊曞▓?
            int estimatedSize=2000+s.fields.size()*200;
            StringBuilder sb=new StringBuilder(estimatedSize);
            sb.append("package ").append(pkg).append(";\n\n");
            sb.append("import io.netty.buffer.ByteBuf;\n");
            sb.append("import java.util.*;\n");
            // SIMD濠电姷鏁告慨鐑藉极閸涘﹥鍙忛柣鎴ｆ閺嬩線鏌涘☉姗堟敾闁告瑥绻橀弻锝夊箣閿濆棭妫勯梺鍝勵儎缁舵岸寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ゆい顓犲厴瀵鏁愭径濠勭杸濡炪倖甯婄拋鏌ュ几濞嗘挻鈷戠紓浣姑粭鈺佲攽椤斿搫鈧骞戦姀鐘闁靛繒濮撮懓鍨攽閳藉棗鐏ユい鏇嗗懎鏋堢€广儱顦伴悡鐔兼煟閺傛寧鎲搁柟铏礈缁辨帡鎮╅搹顐㈢３濡ょ姷鍋涢崯顐ョ亙闂佸憡渚楅崰妤€鈻嶅鍫熺厵闁兼祴鏅炶棢闂佸憡鎸荤换鍫ュ箖濡警鍚嬪璺侯儌閹锋椽姊洪崨濠勭畵閻庢凹鍘介崚濠囨偂楠炵喓鎳撻…銊︽償濠靛牏娉挎俊鐐€ら崑鍕崲濮椻偓楠炴牠宕烽鐔锋瀭闂佸憡娲﹂崑鍡氥亹閹绢喗鈷掑ù锝呮啞閹牓鎮跺鐓庝喊鐎规洘娲栫叅妞ゅ繐瀚崝锕€顪冮妶鍡楃瑐缂佸灈鈧枼鏋旀繝濠傜墛閻撴稓鈧厜鍋撻悗锝庡墰琚ｇ紓鍌欒兌婵敻鎯勯姘煎殨妞ゆ帒瀚崹鍌涖亜閺冨洤袚闁搞倕鐗撳濠氬磼濞嗘劗銈板銈嗘礃閻楃姴鐣烽幎绛嬫晬婵犲﹤瀚惔濠傗攽閻樼粯娑фい鎴濇嚇瀵憡绗熼埀顒勫蓟閻旂厧绀堢憸蹇曟暜濞戙垺鐓曢悗锝庡亜婵秹鏌＄仦鍓р槈闁宠姘︾粻娑㈡晲閸犺埇鍔戝娲焻閻愯尪瀚板褍顭烽弻娑㈠箻鐠虹儤鐏堝Δ鐘靛仜閸燁偉鐏冮梺鍛婁緱閸ㄦ壆绮婇敃鍌涒拺闁告捁灏欓崢娑㈡煕閻樺磭澧柟渚垮妽閹棃濡搁敂瑙勫闂備浇宕甸崰鎰熆濡綍锝囩磼濡晲绨婚梺鐟扮摠缁诲啴宕甸崶顒佺厓闁靛鍨抽悾鐢碘偓瑙勬礀閻栧ジ銆佸Δ鍛劦妞ゆ帒瀚崑鍌涚箾閹存瑥鐏柣鎾存礋閹﹢鎮欓幍顔炬毌闂佸吋绁撮弲鐐存叏閸愭祴鏀介柣妯虹－椤ｆ煡鏌嶉柨瀣伌闁诡喖鍢查埢搴ょ疀閹垮啩鎮ｆ俊鐐€戦崕鏌ュ礉濞嗘挸钃熼柨鐔哄Т瀹告繃銇勯弮鍌氫壕闁告帗鐩幃妤冩喆閸曨剛顦ㄧ紓浣筋嚙閻楀﹥绌辨繝鍥ч唶闁哄洨鍋涢懓鍧楁⒑瑜版帒浜板ù婊呭仱閹嫭鎯旈妸锔规嫽婵炶揪绲块幊鎾活敋濠婂嫭鍙忓┑鐘插亞閻撹偐鈧娲橀崹鍧楃嵁濡ソ铏圭磼濡粯鐝旈梺璇查濠€閬嶁€﹂崼銉﹀仒闁绘俺鍋愮槐鎾诲磼濮橆兘鍋撻幖浣哥９闁归棿绀佺壕褰掓煙闂傚顦︾痪鎯х秺閺岀喖姊荤€靛壊妲紒鐐劤缂嶅﹪寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ょ紓宥咃躬瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁绘劦鍓欓崝銈囩磽瀹ュ拑韬€殿喖顭烽弫鎰緞婵犲嫷鍚呴梻浣瑰缁诲倿骞夊☉銏犵缂備焦顭囬崢杈ㄧ節閻㈤潧孝闁稿﹤缍婂畷鎴﹀Ψ閳哄倻鍘搁柣蹇曞仩椤曆勬叏閸屾壕鍋撳▓鍨灍闁瑰憡濞婇獮鍐ㄢ枎瀵版繂婀遍埀顒婄秵娴滄瑦绔熼弴銏♀拺闁告稑锕︾紓姘舵煕鎼淬倖鐝紒瀣槸椤撳吋寰勭€ｎ剙骞愬┑鐘灱濞夋盯鏁冮敃鈧～婵嬪Ω閳哄倻鍘搁梺閫炲苯澧紒鍌涘笧閳ь剨缍嗛崑鍡涘储閽樺鏀介柍钘夋閻忋儲绻涢崪鍐М闁轰礁绉撮濂稿幢閹邦亞鐩庨梻浣瑰缁诲倸螞濞戙垹鐭楅柍褜鍓熷娲传閸曨剚鎷辩紓浣割儐鐢偤骞戦姀鐘斀閻庯綆浜為敍婊冣攽閻樻墠鍫ュ磻閹惧墎纾兼い鏃傚亾閺嗩剚鎱ㄦ繝鍐┿仢婵☆偄鍟埥澶娾枎閹邦厼鈧兘姊虹拠鎻掝劉缁炬澘绉撮…鍨潨閳ь剟銆佸鑸垫櫜闁糕剝鐟ù鍕煟鎼搭垳绉甸柍褜鍓﹂崢鎼佸疾濠靛绠熼柛娑橈工缁剁偤鎮楅敐搴′簻鐎殿喖娼″娲箹閻愭彃濮岄梺鍛婃煥濞村嘲顕ｈ閸┾偓妞ゆ巻鍋撻柍瑙勫灴閹瑩鎳犻鈧。娲⒑鐠囪尙绠茬紒璇茬墦楠炲啫顫滈埀顒勫极閹剧粯鍋愮€规洖娲ら獮宥夋⒒娴ｅ憡鍟炵紒瀣灴閺佸啴濡舵径濠勫幒闂佸搫娲ㄦ慨顓㈠磻閹炬枼鏋旈柛顭戝枟閻忓牆顪冮妶搴″箻闁稿繑锕㈤悰顕€宕橀纰辨綂闂侀潧鐗嗛幊鎰八囪濮婅櫣绱掑Ο璇茬缂備降鍔岄悥鐓庣暦閹达箑绠荤紓浣诡焽閸欏棝鏌ｆ惔顖滅シ闁告柨顑囬懞杈ㄧ節濮橆厸鎷洪梺鑽ゅ枔婢ф骞嗛崼銏㈢＜濠㈣泛鑻崢瀵糕偓瑙勬礃椤ㄥ懘鎮惧┑瀣劦妞ゆ帒鍊归～鏇㈡煙閻戞ɑ鐓涢柛瀣崌閺佹劖鎯斿┑鍫濆毈闂備胶鎳撻崥瀣礉濞嗘挸钃熺€广儱娲﹂崰鍡涙煕閺囥劌浜炲ù鐓庣焸濮婅櫣鎷犻垾铏亶闂佽崵鍟块弲鐘绘偘椤曗偓楠炴鎷犻懠顒夊敽闂備礁婀遍崑鎾诲几婵傜纾婚柟鐐た閺佸秹鏌ｉ幇顔克夐柟閿嬫そ濮婅櫣娑甸崨顓濇睏闂佺顑嗙粙鎺撶┍婵犲洤绀傞柤娴嬫櫇椤旀洟鏌ｈ箛鎾剁闁绘鍨垮畷鎴﹀箻鐠囪尙鐤€婵炶揪绲介幉锟狀敇閸ф鈷戦柤濮愬€曞瓭濠电偠顕滄俊鍥╁垝婵犲洤绾ч柟宕囶劜缂嶄礁鐣锋總绋课ㄩ柨鏃€鍎崇敮楣冩⒒婵犲骸浜滄繛璇х畱鐓ら柡宓嫭鐦庨梻鍌氬€烽悞锕€顪冮崸妤€纾婚柛鏇ㄥ幐閸嬫挸顫濋鐔哄嚒濡炪値鍋勭换姗€骞栬ぐ鎺戞嵍妞ゆ挾濮烽崢顖炴⒒娴ｅ憡璐℃い顓炵墢閳ь剚绋堥弲鐘荤嵁韫囨稑纭€闁绘垵妫欑€靛矂姊洪棃娑氬婵☆偅鐟х划鍫ュ礃椤旂晫鍘靛┑顔界箓閺堫剟鎮甸鍫熺厵濞撴艾鐏濇慨鍌炴煕閳规儳浜炬俊鐐€栫敮鎺斺偓姘煎弮閹ょ疀濞戞瑧鍘卞銈嗗姧缁茶法绮婚弽銊ｄ簻闁靛／鍐ｆ瀰闂佸搫鏈粙鎴︼綖濠婂牆鐒垫い鎺嗗亾妞ゎ厼娲╅ˇ褰掓煃閵夛附顥堢€规洘锕㈤、娆撳床婢诡垰娲﹂悡鏇㈡煃閳轰礁寮存慨妯挎硾缁狀垰鈹戦悩宕囶暡闁抽攱鍨块弻娑樷攽閸℃浠炬繝娈垮灠閵堟悂寮诲☉姘ｅ亾閿濆簼鎮嶉棅顒夊墯椤ㄣ儵鎮欓崣澶樻闂佷紮绲剧换鍫濈暦閻旂⒈鏁冮柕鍫濋閺佽棄鈹戦悩鑼闁哄绨遍崑鎾诲箻缂佹ê娈戦梺鍓插亝濞叉牠宕掗妸鈺傜厵闂傚倸顕崝宥夋煟閹捐泛鏋涢柣鎿冨亰瀹曞爼濡搁敂缁㈡К濠电偛顕慨宥夊炊閵娧冨箰闂備礁鎲＄划鍫㈢矆娴ｅ湱顩插ù鐓庣摠閻撴瑦銇勯弮鈧崕铏閿斿浜滈柨鏃€鍎抽。濂告煙椤栨稒顥堥柡浣瑰姍瀹曡埖顦版惔锛勫搸闂傚倸鍊搁崐鐑芥嚄閸撲礁鍨濇い鏍仦閺咁亞绱撴担绋库挃闁惧繐閰ｅ畷銏ゆ嚃閳轰礁鐤鹃梻浣筋嚙鐎涒晝绮欓幒妤佹櫇闁宠桨璁查弸鏂棵归悩宸剱闁绘挾鍠栭弻鐔兼焽閿曗偓閻忥紕鎲搁幎濠傛处閻撶娀鏌ｅΔ鈧悧鍡涱敋濠婂嫨浜滈柕濠忕到閸旓箓鏌熼鐣屾噰鐎规洩绲惧鍕節鎼粹懣鐔兼⒒閸屾瑨鍏岄柟铏崌瀹曟煡寮婚妷銉х枃濠碘槅鍨靛▍锝夊汲閿曞倹鐓忓┑鐘茬箰椤╊剛绱掗埦鈧崑鎾绘⒑绾懎浜归悶娑栧劦瀹曟粌鈹戠€ｎ亝杈堥梺闈涚箞閸婃牠鍩涢幒妤佺厱閻忕偞宕樻竟姗€鏌嶈閸撴岸骞冮崒姘辨殾闁圭増婢樼粻鐟懊归敐鍛喐闁挎稒鐟╅幃妤呮偡閺夋浠鹃梺闈╃悼椤ユ劙濡甸幇鏉跨闁瑰濮撮埀顒傚仜椤啴濡堕崱妤€娼戦梺绋款儐閹歌顭囩拠娴嬫斀閻庯綆鍋€閹锋椽姊绘笟鍥т簽闁稿鐩幊鐔碱敍濞戞瑦鐝烽梺鍦檸閸犳鎮″☉銏″€堕柣鎰絻閳锋棃鏌曢崱妯烘诞闁哄苯绉烽¨渚€鏌涢幘鍗炲缂佽京鍋ゅ畷鍗炩槈濡》绱遍梻浣告啞娓氭宕归鐐村€垮┑鐘蹭紖瑜版帗鍋傞幖杈剧稻閹插ジ姊虹紒妯诲碍缂佺粯锕㈠璇测槈閵忕姈銊╂煙鐎涙绠撻柡瀣ㄥ€曢湁闁绘挸楠搁弳锝夋煙椤旂瓔娈旈柍缁樻崌瀹曞綊顢欓悾灞肩敖缂傚倸鍊风欢锟犲窗濡ゅ懏鍋￠柨鏃傛櫕閳瑰秴鈹戦悩鎻掍簽婵炲吋澹嗛埀顒€鍘滈崑鎾斥攽閻樻彃鏁柕澶涘缁♀偓缂佸墽澧楅敋濠⒀嗗皺閹叉悂寮堕崹顔芥闂佽鍟崶褏顔掗梺褰掝暒閻掞妇绱炴惔銊︹拺缂備焦锚閻忥箓鏌ㄥ顓滀簻闁哄浄绻濋崫铏圭磼缂佹娲寸€规洖宕灃闁告劦浜堕崬鐑樼節瀵版灚鍊ら崵瀣磽瀹ュ嫮顦︽い顐㈢箰鐓ゆい蹇撴媼濡啴姊洪崘鍙夋儓闁哥喐濯介崐瀵哥磽閸屾艾鈧悂宕愰崫銉х煋鐎规洖娲ㄩ惌鍡椼€掑锝呬壕濡炪們鍨哄畝鎼佸极閹版澘骞㈡繛鍡樺灩濡插洦绻濆▓鍨灍闁挎洍鏅犲畷鏇㈡偨缁嬭法鍘搁梺鍛婁緱閸犳岸宕㈤鍛瘈闁靛骏绲剧涵楣冩煟濡も偓濡繈骞冨鈧、鏃堝川椤愶紕鐩庢俊鐐€栭崝鎴﹀垂鐠囪尙鎽ラ梻鍌欑閻ゅ洭锝炴径鎰瀭闁秆勵殔閺勩儵鏌曟径鍡樻珔妤犵偑鍨烘穱濠囧Χ閸涱厽娈ㄩ梺鍛婄墬閻楃姴顫忕紒妯诲闁告稑锕ら弳鍫濐渻閵堝骸骞栨俊顐ｇ箓閻ｇ兘宕奸弴鐐嶃劑鏌曡箛鏇炐″瑙勬礋濮婅櫣绮欑捄銊т紘闂佺顑嗛惄顖炵嵁閹达附鍤嬮柣鎰扳偓娑氱泿闂備浇顫夊畷妯衡枖濞戞ɑ姣勯梻鍌欑劍鐎笛呯矙閹烘梹宕查柛鎰典簼瀹曞弶绻涢幋娆忕仼缂佺姾宕甸惀顏堝箚瑜滈崕蹇斻亜閵夛妇绠撻柍瑙勫灴閹瑩鎳犻浣稿瑎闂備胶顭堥敃銉ф崲閸岀儐鏁嬮柨婵嗘川閻瑩骞栫€涙ɑ灏版い顐㈢Ч濮婃椽妫冨☉姘辩暰濠碉紕瀚忛崶褏顔嗗┑鐐叉▕娴滄繈鎮￠悢鍏肩厪闊洤锕ュ▍鍛存煟韫囨稐鎲鹃柡灞剧洴閹晠宕橀幓鎺撴嚈闁诲氦顫夊ú妯煎枈瀹ュ洠鍋撴担鍐ㄤ汗闁逞屽墯缁嬫帡鈥﹂崶顒€鍌ㄥù鐘差儐閳锋垿鏌熺粙鎸庢崳缂佺姵鎸荤换娑氫沪閸屾埃鍋撳┑鍡欐殾闁靛繈鍊曠粻缁樸亜閺冨倹娅曢柛姗€娼ч—鍐Χ閸℃ǚ鎷婚梺鍝勬媼閸嬪﹪骞嗗畝鍕婵°倓鑳堕崢鐢电磽娴ｅ壊鍎忛悘蹇撴嚇瀵劑鎳￠妶鍥╋紲闂佸搫鍟崐鎼佸几濞戙垺鐓曢柍鍝勫暙娴犻亶鏌熼鐣屾噰妞ゃ垺顨嗛幏鍛村传閸曨厾袪闂傚倸鍊搁崐椋庣矆娓氣偓楠炴牠顢曢敃鈧€氬銇勯幒鍡椾壕闁绘挶鍊栨穱濠囶敍濠靛棔姹楅梺鍛婎殕瀹€鎼佸蓟閿濆绫嶉柛灞绢殕鐎氭盯姊烘潪鎵槮缂佸鏁婚獮鍫ュΩ閵夘喗寤洪梺绯曞墲椤ㄥ懐绮昏ぐ鎺撯拺缂備焦顭囩粻鏍ㄦ叏婵犲懎鍚规俊鍙夊姍楠炴鈧潧鎽滈幊婵嬫⒑闁偛鑻晶顖滅磼閸屾氨效闁硅櫕绮撻幃钘夆枔閹稿酣鏁滈梻浣藉吹婵儳顩奸妸褎濯伴柨鏇楀亾妞ゎ厼娲浠嬵敃閵堝浄绱冲┑鐐舵彧缂嶁偓闁稿鍊块獮瀣倷閹绘帞浜栭梻浣告贡閾忓酣宕板Δ鍛亗闁哄洨鍠撶粻楣冩煙鐎电鍓抽柛蹇ｅ墰閳ь剝顫夐幃鍫曞磿閻㈢钃熼柨鐔哄Т楠炪垺淇婇妶鍛殭妞わ絾妞藉铏光偓鍦濞兼劙鏌涢妸銉﹀仴闁靛棔绀侀埢搴ㄥ箣閻愬弶鐎梻浣告贡閸庛倝骞愭ィ鍐╁剹闁圭儤鏌￠崑鎾舵喆閸曨剛顦ㄧ紓浣筋嚙閻楁捇鎮伴閿亾閿濆簼绨撮柡鈧禒瀣厵闂侇叏绠戞晶顖涖亜閹惧磭浠㈤柍瑙勫灴閹晠宕归锝嗙槑濠电姵顔栭崰姘跺极婵犳艾绠犳繝闈涱儐閸婇攱銇勯幒宥囶槮闁告柨鎳忕换婵嬫偨闂堟刀銏＄箾鐠囇呯暤闁诡喒鈧枼鏋庨柟鐐綑娴狀垶姊虹拠鈥冲箺閻㈩垱甯楁穱濠囨嚃閳哄啰锛滈梺鎼炲妽缁嬫帒鈻嶆繝鍐╁弿濠电姴鍋嗛悡鑲┾偓瑙勬礃鐢帡锝炲┑瀣垫晞闁芥ê顦竟鏇㈡⒑瑜版帗锛熺紒鈧担鍝勵棜闁荤喖鍋婂〒濠氭倵閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；闁绘劗鍎ら崑瀣煟濡崵婀介柍褜鍏涚欢姘嚕閹绢喖顫呴柍鈺佸暞閻濇牠姊绘笟鈧埀顒傚仜閼活垱鏅堕幘顔界厵妞ゆ柨鍚嬮崑銉︺亜閵忊€冲摵闁糕斁鍋撳銈嗗笒鐎氼剟鎷戦悢鍏肩厽闁哄啫鍊哥敮鍓佺磼閳ь剟宕煎婵嗙秺閺佹劙宕惰婵℃椽姊洪悷鏉跨骇闁挎洏鍨归～蹇涙倻濡顫￠梺瑙勵問閸ｎ喖危椤曗偓濮婃椽骞愭惔銏狀槱婵炲瓨绮犳禍婊堫敋閿濆棛绡€婵﹩鍘兼禍婊堟⒑缁嬭法绠洪柛瀣姍瀹曟繈鎮滈懞銉㈡嫼闂佸湱顭堢€涒晝澹曢悽鍛婄厱閻庯綆鍋呭畷宀勬煛瀹€瀣？闁逞屽墾缂嶅棝宕滃▎鎾冲嚑闁硅揪闄勯悡?
            if(SIMD_ENABLED && s.hot && hasPrimitiveArrayField(s.fields)){
                sb.append("import jdk.incubator.vector.*;\n");
                sb.append("import java.lang.foreign.MemorySegment;\n");
                sb.append("import java.nio.ByteOrder;\n");
            }
            sb.append("import ").append(JavaRuntimeSupport.bytesPackage(pkg)).append(".*;\n");
            sb.append("import ").append(JavaRuntimeSupport.serializePackage(pkg)).append(".*;\n\n");
            sb.append("public class ").append(s.name).append(" implements ICursorProto {\n");
            if(s.hot){
                sb.append("    // @hot: expand hot container loops and avoid generic dispatch overhead.\n");
                // SIMD闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻锝夊箣閿濆憛鎾绘煕婵犲倹鍋ラ柡灞诲姂瀵挳鎮欏ù瀣壕鐟滅増甯掔壕鍧楁煙鐎电校闁哥姵鍔欓弻锝呂旈埀顒勬偋閸℃瑧绠旈柟鐑橆殕閻撴盯鏌涢弴妤佹珔闁告棑绠撻弻锛勪沪閸撗勫垱閻庢鍠楅幐铏繆閹间礁唯鐟滃矂宕Δ鍛拻濞达綀顫夐崑鐘绘煕閺傝法鐒哥€规洘鍔欏畷褰掝敋閸涱喚绋佹繝鐢靛仜濡﹥绂嶉崼鏇炴瀬闁糕剝绋掗悡鍐喐濠婂牆绀堟繛鎴炶壘閸ㄦ繈鏌￠崘銊モ偓鐢稿磻閹剧粯顥堟繛鎴炵懄閸犳劖绻涢幋鐐村碍缂佸缍婂濠氬灳閹颁礁鎮戦柟鑲╄ˉ閳ь剙纾鎴︽⒒娴ｈ櫣甯涢柟姝岊嚙鐓ゆ繝濠傜墕閻掑灚銇勯幒鍡椾壕闂佸憡蓱缁挸鐣烽幋锕€绠荤紓浣诡焽閸樻捇鎮峰鍕煉鐎规洘绮撻幃銏☆槹鎼淬垺顔曢梻浣烘嚀閻°劎缂撻幆顬綁宕奸妷锔惧帾闂婎偄娲ら敃銉╊敁閸℃稒鐓欓柣銈庡灱閸ゆ瑦銇勯鍕殻濠碘€崇埣瀹曞崬鈻庤箛濠冨珱闂傚倷鑳堕…鍫ヮ敄閸ヮ剙纾婚柕鍫濐槸缁犳牠鏌ㄩ悢鍝勑ｉ柡鍛絻椤法鎹勬ウ鍨伃閻熸粍婢樼€氭澘顫忓ú顏勭闁绘劖褰冩慨宀勬⒑閸涘﹥鐓ョ紒澶庡煐缁傚秹骞栨笟鍥ㄦ櫍闂侀潧绻嗛埀顒€鍘栫紓鎾翠繆閻愵亜鈧牠鎮уΔ鍛櫔濠电姵顔栭崰鏍磹閸ф钃熼柨娑樺濞岊亪鏌涢幘妞捐閸嬫捇骞掑Δ浣哄帗閻熸粍绮撳畷婊堝Ω瑜忕粈濠囨煕閳╁啰鈽夌痪鎯ь煼閺屾稑鈽夐崡鐐典户闂佺粯甯掗敃顏堝蓟閿濆顫呴柣妯哄暱閺嗗牓姊虹紒妯诲鞍闁荤啿鏅犲璇测槈濡攱鐎婚棅顐㈡祫缁茬偓鏅ラ梻浣筋嚙缁绘劖顨ヨ箛鏂剧箚闁搞儺鍓欓悞鍨亜閹烘垵鏋ゆ繛鍏煎姈缁绘盯宕ｆ径宀€鐓夐悗瑙勬磻閸楀啿顕ｆ禒瀣垫晣婵犙勫劤娴滄儳霉閿濆懎顥忔繛绗哄姂閺屽秷顧侀柛鎾跺枛閻涱噣宕橀鑲╁幐闂佸憡渚楅崢楣冾敊瀹€鍕拺闁革富鍘奸崝瀣叏婵犲嫮甯涢柣妤€娴风槐鎾诲磼濮橆兘鍋撻幖浣哥９闁归棿绀佺壕褰掓煙闂傚顦︾痪鎯х秺閺岀喖姊荤€靛壊妲紒鐐劤缂嶅﹪寮婚敐澶婄闁挎繂鎲涢幘缁樼厱闁靛牆鎳庨顓㈡煛鐏炶鈧鍒掑▎鎴炲磯闁靛鍊楁す鎶芥⒒娴ｅ憡鍟為柣鐔村劤閹广垹顫滈埀顒€顕ｆ繝姘櫜濠㈣泛锕﹂娲⒑閹稿海绠撴俊顐幖铻為柛鏇ㄥ幘绾捐棄霉閿濆洦鍤€濠殿喖鐗婇妵鍕Ω閵夘垵鍚悗娈垮枛椤兘宕规ィ鍐ㄧ疀濞达絽鎲￠崐顖炴⒑绾懎浜归悶娑栧劦閸┾偓妞ゆ巻鍋撶痪缁㈠弮椤㈡瑩骞囬悧鍫氭嫽婵炶揪绲介幉锟犲箚閸喆浜滈柟瀛樼箓椤忣參鏌熼搹顐疁鐎规洖銈稿鎾倷闂堟稑袝濠碉紕鍋戦崐鏍ь啅婵犳艾纾婚柟鎯у绾惧ジ鏌ｅΟ鍨毢閺佸牓鎮楃憴鍕婵炶尙鍠栧濠氬幢濡ゅ啯娈奸梺闈涱槶閸庢煡宕板Ο姹囦簻闁靛骏绱曞ú鎾煙椤旂晫鎳呴柍褜鍓涢弫鎼佲€﹂崼銏℃殰闁圭儤顨嗛埛鎺懨归敐鍫綈闁稿濞€閺屾稒鎯旈鑲╀桓閻庤娲樼换鍐箚閺冨牊鏅查柛娑卞帨濮樿埖鈷戦梺顐ゅ仜閼活垱鏅堕婊呯＜缂備焦顭囩粻鐐烘煙椤旇崵鐭欐俊顐㈠暙闇夐棅顒佸絻閻忔煡鏌＄仦鍓р姇缂佺粯绻堝畷鎺戭煥閸涘懌鍔戦幃妤冩喆閸曨剛顦┑鐐叉噺濞叉粎鍒掔€ｎ喖绠抽柡鍌氭惈娴滈箖鏌ㄥ┑鍡涱€楀ù婊呭仱閺屾稑螣閸︻厾鐓撳┑顔硷龚濞咃綁骞夐幘顔肩妞ゆ劑鍨硅闂傚倷绀侀幗婊堝窗鎼粹槅鐒介柨鐔哄Т閺勩儵鏌嶈閸撴岸濡甸崟顖氱闁糕剝銇炴竟鏇㈡⒒娴ｇ瓔鍤欑紒缁樺灩閹广垽宕奸妷锔芥珳闂佺粯鍔曟晶搴ㄦ偪閳ь剟姊洪悷鏉跨稏闁绘帪绠戦—鍐╃鐎ｃ劉鍋撴笟鈧顕€宕煎┑鍥ヤ虎濠电偠鎻紞鈧い鏇熺墪铻為柛鎰靛枟閳锋帡鏌涚仦鍓ф噮妞わ讣绠撻弻娑㈡偄闁垮浠村Δ鐘靛仦閻楃娀銆侀弴銏犖ч柛灞剧煯婢规洖鈹戦绛嬬劷闁告鍐ｆ瀺鐎广儱顦扮€电姵绻濋棃娑欑仼鐟滅増甯楅崑鍌炲箹鏉堝墽绋婚柟鎻掋偢濮婃椽骞庨懞銉︽殸闂佹悶鍔屽鈥愁嚕鐠囨祴妲堟俊顖炴敱椤秴鈹戦绛嬫當闁绘锕顐ｃ偅閸愨斁鎷虹紓鍌欑劍钃遍悘蹇ｄ邯閺屾稒鎯旈敐鍡樻瘓閻庢鍠栭…鐑藉极閹邦厼绶炴俊顖滅帛濞呭矂姊绘担渚劸闁哄牜鍓熼幊婵嬫倷椤掑偆娴勫┑顔姐仜閸嬫捇鏌＄仦鍓р槈閾绘牠鏌涘☉鍗炲闁哥姵宀稿娲传閸曨厾浼囬梺鍝ュУ閻楃娀鐛崘顓ф▌閻庤娲栭妶鎼佸箖閵忋倕浼犻柛鏇ㄥ亝椤撹偐绱撻崒姘偓鎼佸磹閻戣姤鍤勯柤鎼佹涧閸ㄦ棃鎮楅棃娑欐喐缂佲偓婵犲倶鈧帒顫濋敐鍛闁诲孩顔栭崰鏍€﹂柨瀣╃箚闁归棿绀侀悡娑樏归敐鍥剁劸缂佷緡鍠楃换婵嗩嚗闁垮绶查柍褜鍓氶崝娆忕暦閹达箑绠婚悺鎺嶈兌閹虫捇鈥﹂妸鈺侀唶闁靛繈鍨哄В鍥⒒娴ｅ憡鎯堥柣顓烆槺缁辩偞绗熼埀顒勭嵁閸愵喖顫呴柕鍫濇噸缁卞爼姊洪棃娑辨缂佺姵鍨甸埢鎾圭疀閺囩姷锛濋梺绋挎湰閻熴劑宕楃仦瑙ｆ斀妞ゆ梻鍋撻弳顒侇殽閻愭潙鐏寸€规洘鍎奸¨渚€鏌涢妶鍡樼闁宠鍨块幃鈺佺暦閸ヨ埖娈归梻浣告惈閹冲繗銇愰崘顔光偓锔炬崉閵婏箑纾繝闈涘€块幗顏堝Χ鎼粹懇鏋岄梻鍌欑閹碱偊寮甸鍕剮妞ゆ牗绋愮换鍡涙煟閹达絽袚闁哄懏鐓￠弻娑樷槈閸楃偛瀛ｉ梺璇插婵炲﹤顫忕紒妯诲闁惧繒鎳撶粭锛勭磽娴ｇ瓔鍤欓悗姘煎灦閹娆㈤崸妤佲拻闁稿本鑹鹃埀顒佹倐瀹曟劙骞栨担鍝ワ紮闂佸綊妫跨粈浣哄瑜版帗鐓欓梻鍌氼嚟椤︼妇鐥崜褎鍤€妞ゎ亜鍟伴埀顒婄秵娴滄繈骞戦敐澶嬬厽妞ゆ挾鍋為ˉ婊堟煏閸℃ê绗掓い顐ｇ箞閺佹劙宕ㄩ鈧ˉ姘攽鎺抽崐妤佹叏閻戣棄纾婚柣鎰劋閸嬪鏌ｅΟ鑽ゆ菇闁逞屽厸缁舵艾顕ｉ幘顔碱潊闁斥晛鍟悵鏍⒒娓氣偓閳ь剛鍋涢懟顖涙櫠閹绢喗鐓欐い鏂垮悑閸嬨儲銇勯姀鈥冲摵闁糕斁鍋撳銈嗗笒鐎氼剟鎷戦悢鍏肩厽闁哄啫鍊哥敮鍓佺磼閳ь剟宕煎婵嗙秺閺佹劙宕惰婵℃椽姊洪悷鏉跨骇闁挎洏鍨归～蹇涙倻濡顫￠梺瑙勵問閸ｎ喖危椤曗偓濮婃椽骞愭惔銏狀槱婵炲瓨绮犳禍婊堫敋閿濆棛绡€婵﹩鍘兼禍婊堟⒑缁嬭法绠洪柛瀣姍瀹曟繈鎮滈懞銉㈡嫼闂佸湱顭堢€涒晝澹曢悽鍛婄厱閻庯綆鍋呭畷宀勬煛瀹€瀣？闁逞屽墾缂嶅棝宕滃▎鎾冲嚑闁硅揪闄勯悡?
                if(SIMD_ENABLED && hasPrimitiveArrayField(s.fields)){
                    sb.append("    // SIMD support constants for primitive-array fast paths.\n");
                    sb.append("    private static final VectorSpecies<Byte> BYTE_SPECIES = ByteVector.SPECIES_256;\n");
                    sb.append("    private static final VectorSpecies<Integer> INT_SPECIES = IntVector.SPECIES_256;\n");
                    sb.append("    private static final VectorSpecies<Long> LONG_SPECIES = LongVector.SPECIES_256;\n");
                    sb.append("    private static final VectorSpecies<Float> FLOAT_SPECIES = FloatVector.SPECIES_256;\n");
                    sb.append("    private static final VectorSpecies<Double> DOUBLE_SPECIES = DoubleVector.SPECIES_256;\n");
                    sb.append("    private static final VectorSpecies<Integer> FIXED_INT_SPECIES = INT_SPECIES;\n");
                    sb.append("    private static final VectorSpecies<Long> FIXED_LONG_SPECIES = LONG_SPECIES;\n");
                    sb.append("    private static final VectorSpecies<Float> FIXED_FLOAT_SPECIES = FLOAT_SPECIES;\n");
                    sb.append("    private static final VectorSpecies<Double> FIXED_DOUBLE_SPECIES = DOUBLE_SPECIES;\n");
                }
            }
            for(Field f: s.fields){
                sb.append("    ").append(mapType(f)).append(" ").append(f.name).append(";\n");
            }
            for(Field f: s.fields){
                String t=mapType(f);
                String fname=f.name;
                sb.append("    public ").append(t).append(" get").append(cap(fname)).append("(){return ").append(fname).append(";}\n");
                sb.append("    public void set").append(cap(fname)).append("(").append(t).append(" v){this.").append(fname).append("=v;}\n");
            }
            appendJavaEstimatedSizeMethod(sb, s, presenceFields, false);
            appendJavaProjectionType(sb, s);
            appendJavaReadFromMethodBody(sb, s, presenceFields, "ByteBuf", "buf");
            appendJavaReadIntoMethodBody(sb, s, presenceFields, "ByteBuf", "buf");
            appendJavaProjectedReadMethodBody(sb, s, presenceFields);
            appendJavaSkipMethodBody(sb, s, presenceFields);
            if(s.hot){
                appendJavaSkipManyMethodBody(sb, s);
            }
            appendJavaReadFromMethodBody(sb, s, presenceFields, "ByteCursor", "input");
            appendJavaReadIntoMethodBody(sb, s, presenceFields, "ByteCursor", "input");
            appendJavaWriteMethodBody(sb, s, presenceFields, "ByteBuf", "buf");
            appendJavaWriteMethodBody(sb, s, presenceFields, "ByteCursor", "output");
            sb.append("}\n");
            return sb.toString();
        }
        static String generateFixedStruct(String pkg, Struct s){
            StringBuilder sb=new StringBuilder(1600+s.fields.size()*160);
            sb.append("package ").append(pkg).append(";\n\n");
            sb.append("import io.netty.buffer.ByteBuf;\n");
            sb.append("import java.util.*;\n");
            if(SIMD_ENABLED && hasFixedSimdArrayField(s.fields)){
                sb.append("import jdk.incubator.vector.*;\n");
                sb.append("import java.lang.foreign.MemorySegment;\n");
                sb.append("import java.nio.ByteOrder;\n");
            }
            sb.append("import ").append(JavaRuntimeSupport.bytesPackage(pkg)).append(".*;\n");
            sb.append("import ").append(JavaRuntimeSupport.serializePackage(pkg)).append(".*;\n\n");
            sb.append("public class ").append(s.name).append(" implements ICursorProto {\n");
            sb.append("    // @fixed: fixed-layout codec without presence bits.\n");
            if(s.inline){
                sb.append("    // @inline: nested reads and writes are inlined when embedded.\n");
            }
            if(SIMD_ENABLED && hasFixedSimdArrayField(s.fields)){
                sb.append("    private static final VectorSpecies<Integer> FIXED_INT_SPECIES = IntVector.SPECIES_PREFERRED;\n");
                sb.append("    private static final VectorSpecies<Long> FIXED_LONG_SPECIES = LongVector.SPECIES_PREFERRED;\n");
                sb.append("    private static final VectorSpecies<Float> FIXED_FLOAT_SPECIES = FloatVector.SPECIES_PREFERRED;\n");
                sb.append("    private static final VectorSpecies<Double> FIXED_DOUBLE_SPECIES = DoubleVector.SPECIES_PREFERRED;\n");
            }
            for(Field f: s.fields){
                sb.append("    ").append(mapType(f)).append(" ").append(f.name).append(";\n");
            }
            for(Field f: s.fields){
                String t=mapType(f);
                String fname=f.name;
                sb.append("    public ").append(t).append(" get").append(cap(fname)).append("(){return ").append(fname).append(";}\n");
                sb.append("    public void set").append(cap(fname)).append("(").append(t).append(" v){this.").append(fname).append("=v;}\n");
            }
            appendJavaEstimatedSizeMethod(sb, s, java.util.Collections.emptyList(), true);
            appendJavaProjectionType(sb, s);
            appendJavaFixedReadFromMethodBody(sb, s, "ByteBuf", "buf");
            appendJavaFixedReadIntoMethodBody(sb, s, "ByteBuf", "buf");
            appendJavaProjectedFixedReadMethodBody(sb, s);
            appendJavaSkipFixedMethodBody(sb, s);
            appendJavaFixedReadFromMethodBody(sb, s, "ByteCursor", "input");
            appendJavaFixedReadIntoMethodBody(sb, s, "ByteCursor", "input");
            appendJavaFixedWriteMethodBody(sb, s, "ByteBuf", "buf");
            appendJavaFixedWriteMethodBody(sb, s, "ByteCursor", "output");
            sb.append("}\n");
            return sb.toString();
        }
        static void appendJavaEstimatedSizeMethod(StringBuilder sb, Struct s, List<Field> presenceFields, boolean fixedLayout){
            sb.append("    @Override\n");
            sb.append("    public int estimatedSize(){\n");
            if(fixedLayout){
                sb.append("        int __size=0;\n");
            }else{
                sb.append("        int __size=ByteIO.sizeOfPresenceBits(").append(presenceFields.size()).append(");\n");
            }
            for(Field field: s.fields){
                String valueExpr="this."+field.name;
                if(!fixedLayout && isPresenceTrackedType(field.type)){
                    sb.append("        if(").append(javaHasWireValueExpr(valueExpr, field)).append("){\n");
                    appendJavaEstimateFieldStatements(sb, valueExpr, field, "            ", false);
                    sb.append("        }\n");
                }else{
                    appendJavaEstimateFieldStatements(sb, valueExpr, field, "        ", fixedLayout);
                }
            }
            sb.append("        return __size;\n");
            sb.append("    }\n");
        }
        static void appendJavaEstimateFieldStatements(StringBuilder sb, String valueExpr, Field f, String indent, boolean fixedLayout){
            if(isBorrowedBytesField(f)){
                if(f.fixedLength!=null){
                    sb.append(indent).append("__size += ").append(f.fixedLength).append(";\n");
                }else{
                    sb.append(indent).append("__size += ByteIO.sizeOfBorrowedBytes(").append(valueExpr).append(");\n");
                }
                return;
            }
            if(isBorrowedStringField(f)){
                if(f.fixedLength!=null){
                    sb.append(indent).append("__size += ").append(f.fixedLength).append(";\n");
                }else{
                    sb.append(indent).append("__size += ByteIO.sizeOfBorrowedString(").append(valueExpr).append(");\n");
                }
                return;
            }
            if(isBorrowedPrimitiveArrayField(f)){
                if(f.fixedLength!=null){
                    int scalarBytes=javaFixedSkipScalarBytes(f.type.substring(0, f.type.length()-2).trim());
                    sb.append(indent).append("__size += ").append(f.fixedLength * scalarBytes).append(";\n");
                }else if("int[]".equals(f.type)){
                    sb.append(indent).append("__size += ByteIO.sizeOfBorrowedRawIntArray(").append(valueExpr).append(");\n");
                }else if("long[]".equals(f.type)){
                    sb.append(indent).append("__size += ByteIO.sizeOfBorrowedRawLongArray(").append(valueExpr).append(");\n");
                }else if("float[]".equals(f.type)){
                    sb.append(indent).append("__size += ByteIO.sizeOfBorrowedRawFloatArray(").append(valueExpr).append(");\n");
                }else if("double[]".equals(f.type)){
                    sb.append(indent).append("__size += ByteIO.sizeOfBorrowedRawDoubleArray(").append(valueExpr).append(");\n");
                }else{
                    throw new IllegalArgumentException("unsupported borrowed primitive array field: "+f.type);
                }
                return;
            }
            if(isFixedLengthStringField(f)){
                sb.append(indent).append("__size += ").append(f.fixedLength).append(";\n");
                return;
            }
            if(isFixedCountArrayField(f)){
                int scalarBytes=javaFixedSkipScalarBytes(f.type.substring(0, f.type.length()-2).trim());
                sb.append(indent).append("__size += ").append(f.fixedLength * scalarBytes).append(";\n");
                return;
            }
            if(isPackedPrimitiveListField(f)){
                String inner=genericBody(f.type).trim();
                int scalarBytes=javaFixedSkipScalarBytes(inner);
                String countVar=childVar(valueExpr, "count");
                sb.append(indent).append("int ").append(countVar).append("=").append(valueExpr).append("==null?0:").append(valueExpr).append(".size();\n");
                sb.append(indent).append("__size += ByteIO.sizeOfSize(").append(countVar).append(")+(").append(countVar).append("*").append(scalarBytes).append(");\n");
                return;
            }
            if(isPackedPrimitiveMapField(f)){
                List<String> kv=splitTopLevel(genericBody(f.type), ',');
                int keyBytes=javaFixedSkipScalarBytes(kv.get(0).trim());
                int valueBytes=javaFixedSkipScalarBytes(kv.get(1).trim());
                String countVar=childVar(valueExpr, "count");
                sb.append(indent).append("int ").append(countVar).append("=").append(valueExpr).append("==null?0:").append(valueExpr).append(".size();\n");
                sb.append(indent).append("__size += ByteIO.sizeOfSize(").append(countVar).append(")+(").append(countVar).append("*").append(keyBytes + valueBytes).append(");\n");
                return;
            }
            if(isPackedIntKeyObjectMapField(f)){
                List<String> kv=splitTopLevel(genericBody(f.type), ',');
                String valueType=kv.get(1).trim();
                String countVar=childVar(valueExpr, "count");
                String entryVar=childVar(valueExpr, "entry");
                String valueVar=childVar(valueExpr, "value");
                sb.append(indent).append("int ").append(countVar).append("=").append(valueExpr).append("==null?0:").append(valueExpr).append(".size();\n");
                sb.append(indent).append("__size += ByteIO.sizeOfSize(").append(countVar).append(")+(").append(countVar).append("*4);\n");
                sb.append(indent).append("if(").append(valueExpr).append("!=null){\n");
                sb.append(indent).append("    for(Map.Entry<Integer,").append(mapType(valueType)).append("> ").append(entryVar).append(" : ").append(valueExpr).append(".entrySet()){\n");
                sb.append(indent).append("        ").append(mapType(valueType)).append(" ").append(valueVar).append("=").append(entryVar).append(".getValue();\n");
                appendJavaEstimateValueStatements(sb, valueVar, valueType, indent+"        ");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(!fixedLayout && isOptionalType(f.type)){
                String inner=genericBody(f.type).trim();
                String innerVar=childVar(valueExpr, "value");
                sb.append(indent).append(mapType(inner)).append(" ").append(innerVar).append("=").append(valueExpr).append(".get();\n");
                appendJavaEstimateValueStatements(sb, innerVar, inner, indent);
                return;
            }
            if(fixedLayout){
                appendJavaFixedEstimateValueStatements(sb, valueExpr, f.type, indent);
            }else{
                appendJavaEstimateValueStatements(sb, valueExpr, f.type, indent);
            }
        }
        static void appendJavaEstimateValueStatements(StringBuilder sb, String valueExpr, String t, String indent){
            if(t.equals("int") || t.equals("Integer")){
                sb.append(indent).append("__size += ByteIO.sizeOfInt(").append(valueExpr).append(");\n");
                return;
            }
            if(t.equals("long") || t.equals("Long")){
                sb.append(indent).append("__size += ByteIO.sizeOfLong(").append(valueExpr).append(");\n");
                return;
            }
            if(t.equals("byte") || t.equals("Byte")){
                sb.append(indent).append("__size += 1;\n");
                return;
            }
            if(t.equals("short") || t.equals("Short")){
                sb.append(indent).append("__size += ByteIO.sizeOfShort(").append(valueExpr).append(");\n");
                return;
            }
            if(t.equals("boolean") || t.equals("Boolean")){
                sb.append(indent).append("__size += 1;\n");
                return;
            }
            if(t.equals("char") || t.equals("Character")){
                sb.append(indent).append("__size += ByteIO.sizeOfChar(").append(valueExpr).append(");\n");
                return;
            }
            if(t.equals("float") || t.equals("Float")){
                sb.append(indent).append("__size += 4;\n");
                return;
            }
            if(t.equals("double") || t.equals("Double")){
                sb.append(indent).append("__size += 8;\n");
                return;
            }
            if(t.equals("String")){
                sb.append(indent).append("__size += ByteIO.sizeOfString(").append(valueExpr).append(");\n");
                return;
            }
            if(ENUMS.contains(t)){
                sb.append(indent).append("__size += ByteIO.sizeOfUInt(").append(valueExpr).append(".ordinal());\n");
                return;
            }
            if(isOptionalType(t)){
                String inner=genericBody(t).trim();
                String innerVar=childVar(valueExpr, "value");
                sb.append(indent).append("__size += 1;\n");
                sb.append(indent).append("if(").append(valueExpr).append("!=null && ").append(valueExpr).append(".isPresent()){\n");
                sb.append(indent).append("    ").append(mapType(inner)).append(" ").append(innerVar).append("=").append(valueExpr).append(".get();\n");
                appendJavaEstimateValueStatements(sb, innerVar, inner, indent+"    ");
                sb.append(indent).append("}\n");
                return;
            }
            if(t.endsWith("[]")){
                String inner=t.substring(0, t.length()-2).trim();
                String countVar=childVar(valueExpr, "count");
                String elemVar=childVar(valueExpr, "elem");
                sb.append(indent).append("int ").append(countVar).append("=").append(valueExpr).append("==null?0:").append(valueExpr).append(".length;\n");
                sb.append(indent).append("__size += ByteIO.sizeOfSize(").append(countVar).append(");\n");
                if("byte".equals(inner) || "Byte".equals(inner) || "boolean".equals(inner) || "Boolean".equals(inner)){
                    sb.append(indent).append("__size += ").append(countVar).append(";\n");
                    return;
                }
                if("float".equals(inner) || "Float".equals(inner)){
                    sb.append(indent).append("__size += ").append(countVar).append("<<2;\n");
                    return;
                }
                if("double".equals(inner) || "Double".equals(inner)){
                    sb.append(indent).append("__size += ").append(countVar).append("<<3;\n");
                    return;
                }
                sb.append(indent).append("if(").append(valueExpr).append("!=null){\n");
                sb.append(indent).append("    for(").append(mapType(inner)).append(" ").append(elemVar).append(" : ").append(valueExpr).append("){\n");
                appendJavaEstimateValueStatements(sb, elemVar, inner, indent+"        ");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isListLikeType(t) || isSetLikeType(t) || isQueueLikeType(t)){
                String inner=genericBody(t).trim();
                String countVar=childVar(valueExpr, "count");
                String elemVar=childVar(valueExpr, "elem");
                sb.append(indent).append("int ").append(countVar).append("=").append(valueExpr).append("==null?0:").append(valueExpr).append(".size();\n");
                sb.append(indent).append("__size += ByteIO.sizeOfSize(").append(countVar).append(");\n");
                sb.append(indent).append("if(").append(valueExpr).append("!=null){\n");
                sb.append(indent).append("    for(").append(mapType(inner)).append(" ").append(elemVar).append(" : ").append(valueExpr).append("){\n");
                appendJavaEstimateValueStatements(sb, elemVar, inner, indent+"        ");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isMapLikeType(t)){
                List<String> kv=splitTopLevel(genericBody(t), ',');
                String keyType=kv.get(0).trim();
                String valueType=kv.get(1).trim();
                String countVar=childVar(valueExpr, "count");
                String entryVar=childVar(valueExpr, "entry");
                String keyVar=childVar(valueExpr, "key");
                String valueVar=childVar(valueExpr, "value");
                sb.append(indent).append("int ").append(countVar).append("=").append(valueExpr).append("==null?0:").append(valueExpr).append(".size();\n");
                sb.append(indent).append("__size += ByteIO.sizeOfSize(").append(countVar).append(");\n");
                sb.append(indent).append("if(").append(valueExpr).append("!=null){\n");
                sb.append(indent).append("    for(Map.Entry<").append(mapType(keyType)).append(",").append(mapType(valueType)).append("> ").append(entryVar).append(" : ").append(valueExpr).append(".entrySet()){\n");
                sb.append(indent).append("        ").append(mapType(keyType)).append(" ").append(keyVar).append("=").append(entryVar).append(".getKey();\n");
                appendJavaEstimateValueStatements(sb, keyVar, keyType, indent+"        ");
                sb.append(indent).append("        ").append(mapType(valueType)).append(" ").append(valueVar).append("=").append(entryVar).append(".getValue();\n");
                appendJavaEstimateValueStatements(sb, valueVar, valueType, indent+"        ");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("}\n");
                return;
            }
            sb.append(indent).append("__size += ").append(valueExpr).append(".estimatedSize();\n");
        }
        static void appendJavaFixedEstimateValueStatements(StringBuilder sb, String valueExpr, String t, String indent){
            int scalarBytes=javaFixedSkipScalarBytes(t);
            if(scalarBytes>0){
                sb.append(indent).append("__size += ").append(scalarBytes).append(";\n");
                return;
            }
            if(t.endsWith("[]")){
                String inner=t.substring(0, t.length()-2).trim();
                String countVar=childVar(valueExpr, "count");
                String elemVar=childVar(valueExpr, "elem");
                sb.append(indent).append("int ").append(countVar).append("=").append(valueExpr).append("==null?0:").append(valueExpr).append(".length;\n");
                sb.append(indent).append("__size += ByteIO.sizeOfSize(").append(countVar).append(");\n");
                int innerFixedBytes=javaFixedSkipScalarBytes(inner);
                if(innerFixedBytes>0){
                    sb.append(indent).append("__size += ").append(countVar).append("*").append(innerFixedBytes).append(";\n");
                    return;
                }
                sb.append(indent).append("if(").append(valueExpr).append("!=null){\n");
                sb.append(indent).append("    for(").append(mapType(inner)).append(" ").append(elemVar).append(" : ").append(valueExpr).append("){\n");
                appendJavaFixedEstimateValueStatements(sb, elemVar, inner, indent+"        ");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("}\n");
                return;
            }
            sb.append(indent).append("__size += ").append(valueExpr).append(".estimatedSize();\n");
        }
        static String generateBO(String protoPkg,String boPkg,String base, Proto p){
            // 濠电姷鏁告慨鐑藉极閸涘﹥鍙忛柣鎴ｆ閺嬩線鏌涘☉姗堟敾闁告瑥绻橀弻锝夊箣濠垫劖缍楅梺閫炲苯澧柛濠傛健楠炴劖绻濋崘顏嗗骄闂佸啿鎼鍥╃矓椤旈敮鍋撶憴鍕８闁告梹鍨甸锝夊醇閺囩偟顓洪梺缁樼懃閹虫劙鐛姀銈嗏拻闁稿本鐟чˇ锕傛煙濞村澧茬紒妤冨枎铻栭柛娑卞幘閻撴垿鏌熼崗鑲╂殬闁告柨绉瑰畷鎴﹀礋椤栨稓鍘遍梺鏂ユ櫅閸橀箖鎳栭埡鍌氬簥闂佺硶鍓濊彠濞存粍绮撻弻鈥愁吋閸愩劌顬夐梺姹囧妽閸ㄥ爼骞堥妸鈺傛櫜闁搞儜鍌涱潟闂備礁鎼張顒傜矙閹捐鐒垫い鎺戯功缁夌敻鏌涚€ｎ亝鍣藉ù婊勬倐椤㈡﹢鎮㈢紙鐘电泿婵＄偑鍊栭崝褏寰婄捄銊т笉闁绘劗鍎ら悡娆愩亜閺冨倹鍤€濠⒀勭叀閺岀喖顢涘☉娆樻闂佺硶鏅粻鎾诲春閳ь剚銇勯幒鎴濐仼缂佺媭鍨遍妵鍕箛閸洘顎嶉梺缁樻尵閸犳牠鐛弽顬ュ酣顢楅埀顒勫焵椤戞儳鈧洟鈥﹂崶顒€绠涙い鎾跺Х椤旀洟姊洪崨濠勬噧妞わ箒浜划濠氭倷閻戞鍙嗗┑鐘绘涧閻楀棙绂掗敂閿亾閸偅绶查悗姘嵆閻涱噣宕堕澶嬫櫌闂佺鏈划宥呅掓惔銊︹拻闁稿本鐟чˇ锕傛煙绾板崬浜扮€规洦鍨堕、鏇㈡晜閽樺缃曢梻浣虹《閸撴繈鏁嬮梺鍛婃⒐濡啫顫忔繝姘＜婵炲棙鍨垫俊浠嬫煟鎼达絿鎳楅柛鎰亾缂嶅酣鎮峰鍛暭閻㈩垱甯炴竟鏇犳崉閵娿垹浜鹃悷娆忓缁€鈧┑鐐额嚋缁犳挸顕ｉ崘宸叆闁割偅绻勯鎰攽閻戝洨绉甸柛鎾寸懄娣囧﹥绂掔€ｎ偆鍘介梺瑙勫礃濞夋盯寮稿☉娆樻闁绘劕顕晶顒佺箾閻撳海绠荤€规洘绮忛ˇ鎾煥濞戞艾鏋涙慨濠勫劋鐎电厧鈻庨幋鐘橈綁姊洪崨濠勬噧闁哥喐娼欓锝囨嫚濞村顫嶅┑鐐叉閸旀洟宕濋崨瀛樷拺闂傚牊渚楅悞楣冩煕婵犲啰澧电€规洘婢橀～婵嬵敄閳哄倹顥堥柟顔规櫊濡啫鈽夊Δ鍐╁礋缂傚倸鍊烽懗鍓佸垝椤栨粍鏆滈柨鐔哄Т閺勩儵鏌嶈閸撴岸濡甸崟顖氱闁规惌鍨版慨娑氱磽娴ｅ壊妲洪柡浣割煼瀵鈽夐姀鈥充汗閻庤娲栧ú銈夊煕瀹€鍕拺閻犲洠鈧櫕鐏堝┑鐐点€嬬换婵嬪Υ娴ｅ壊娼╅悹楦挎閸旓箑顪冮妶鍡楃瑨閻庢凹鍓熼幏鎴︽偄閸濄儳顔曢梺鐟扮摠閻熴儵鎮橀埡鍛埞妞ゆ牗鍑瑰〒濠氭煏閸繃顥為柍閿嬪浮閺屾稑螣閻樺弶绁紓宥嗙墬閵囧嫯绠涢幘璺侯杸闂佹娊鏀遍崹鍧楀蓟閻旂厧绠氶柡澶婃櫇閹剧粯鐓涘〒姘ｅ亾濞存粌鐖煎璇测槈閵忕姈鈺呮煏婢舵稓鐣卞ù鐘虫尦閹鈻撻崹顔界亪濡炪値鍘鹃崗姗€鐛崘顔碱潊闁靛牆妫欓崕顏堟⒑闂堚晛鐦滈柛娆忕箳濡叉劙宕ｆ径宀€鐦堢紒鍓у钃辨い顐躬閺屾盯濡搁敃鈧埢鏇犫偓瑙勬礃濞茬喐淇婇崼鏇炵倞闁靛鍎宠ぐ鎾⒒娴ｈ櫣甯涢柛鏃€顨婂畷鏇㈠Χ婢跺﹦鍘遍梺鐟邦嚟婵澹曢挊澹濆綊鏁愰崼顐㈡異闂佺粯甯婄划娆撳蓟瀹ュ鏁嶆繛鎴炵懅椤︻厾绱撴担浠嬪摵閻㈩垽绻濋妴浣糕枎閹惧磭顦ч梺绋跨箳閸樠囨⒒椤栨稓绡€缁剧増菤閸嬫捇宕橀懠顒勭崜闂備礁鎲″褰掓偡閳哄懏鍋樻い鏇楀亾妤犵偞甯￠獮濠囨惞椤愶綆妫冮梺绯曟杹閸嬫挸顪冮妶鍡楃瑨閻庢凹鍙冨畷鏇炍旀担椋庣畾闂侀潧鐗嗙€氼參藝妞嬪海纾奸悹鍥у级椤ョ偤鏌曢崶褍顏€殿喕绮欐俊姝岊檨闁哄棴绻濆铏规嫚閳ュ磭浠╅梺缁橆殔缁绘帒危閹版澘绠抽柟鎯у閹虫繈姊洪幖鐐插妧闁告洦鍘肩紞鍡涙⒒閸屾瑦绁版い鏇熺墵瀹曟澘螖閸涱偀鍋撻崘顔奸唶闁靛鍎抽悿鍛存⒑閸︻叀妾搁柛鐘崇墱缁牏鈧綆鍋佹禍婊堟煙閻戞ê鐏ュù婊呭仦娣囧﹪鎳犻鈧。鑲╃磼缂佹绠橀柛鐘诧攻瀵板嫬鐣濋埀顒勬晬閻斿吋鈷戠紒瀣儥閸庢劖銇勯鐐村枠鐎规洘宀搁獮鎺楀箣閺冣偓閻庡姊虹憴鍕婵炲绋撶划濠囨晝閸屾稈鎷洪梺鍛婄箓鐎氼噣鍩㈡径鎰厱婵☆垱浜介崑銏☆殽閻愭潙鐏撮柟铏矒閹瑩鏌呭☉姘辨晨闂傚倷娴囬～澶婄暦濡　鏋栨繛鎴欏灩閸戠娀骞栧ǎ顒€濡介柣鎾跺枑缁绘繈妫冨☉娆忔閻庤鎸稿Λ娆撳箞閵婏妇绡€闁告劏鏂傛禒銏ゆ倵濞堝灝娅橀柛鎾跺枑娣囧﹪鎮滈懞銉︽珕闂佷紮绲介懟顖滃緤娴犲鈷掗柛灞剧懅椤︼箓鏌熼懞銉х煉鐎规洘濞婃俊鐑藉煛娴ｅ摜鈧參鏌ｉ悩鐑樸€冮悹鈧敃鍌氬惞闁哄洢鍨洪崐鐢告煕閿旇骞栭崯鎼佹⒑濮瑰洤鈧劙宕戦幘缁樷拻濞达絽鎲＄拹锟犳煣韫囨捇鍙勭€规洖缍婇弻鍡楊吋閸涱噮妫熼梻渚€鈧偛鑻晶瀛樻叏婵犲嫮甯涢柟宄版嚇閹煎綊鎮烽幍顕呭仹闂傚倷绀侀幉鈥愁潖閻熸噴娲冀椤掑倷鑸繝鐢靛Х閺佸憡鎱ㄩ弶鎳ㄦ椽濡舵径濠呅曢悷婊呭鐢鎮￠悢鍏肩厸闁稿本绻冪涵鑸电箾閸儰鎲鹃柡宀嬬節閸┾偓妞ゆ帒瀚崵宥夋煏婢舵稓瀵肩紒銊ヮ煼濮婃椽宕崟顓夌娀鏌涢弬璺ㄧ劯鐎规洜鏁婚、妤呭礋椤掑倸骞堥梻渚€娼ч悧鍡椕洪妶澶婂嚑闁哄啫鐗婇悡鍐喐濠婂牆绀堟繛鍡樻尰閸婅埖鎱ㄥ鍡楀⒒闁绘柨妫欓幈銊ヮ渻鐠囪弓澹曢梻浣芥〃缁€渚€宕幘顔衡偓渚€寮崼婵堫槹濡炪倖鎸鹃崰鎰邦敊閺囩姷纾介柛灞剧懅椤︼附銇勯幋婵堝ⅵ妞ゃ垺宀搁獮搴ㄦ寠婢跺瞼娼夐梻渚€鈧偛鑻晶瀛橆殽閻愯尙绠荤€规洏鍔庨埀顒佺⊕鑿ら柟宄扮秺濮婇缚銇愰幒鎴滃枈闂佸憡鎸婚悷銉暰?000闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕偓浠嬫晸閵夆晛纾婚柕蹇嬪€栭悡鏇㈡煟閹邦垰鐨洪柛鈺嬬稻閹便劍绻濋崘鈹夸虎濠碘槅鍋勯崯顐﹀煡婢跺缍囬柕濞垮灪閻忎線姊婚崒娆戭槮闁硅姤绮嶉幈銊╂偨閹肩偐鍋撻崘鈺冪瘈闁稿被鍊曞▓?+ 婵犵數濮烽弫鍛婃叏閻戣棄鏋侀柛娑橈攻閸欏繘鏌ｉ幋锝嗩棄闁哄绶氶弻娑樷槈濮楀牊鏁鹃梺鍛婄懃缁绘﹢寮婚敐澶婎潊闁绘ê妯婂Λ宀勬⒑鏉炴壆顦﹂柨鏇ㄤ邯瀵鍨鹃幇浣告倯闁硅偐琛ラ埀顒€纾鎰版⒒閸屾艾鈧悂宕戦崱娑樺瀭闂侇剙绉存闂佸憡娲﹂崹浼村礃閳ь剟姊洪棃娴ゆ盯宕ㄩ姘瑢缂傚倸鍊搁崐宄懊归崶鈺冪濞村吋娼欑壕瑙勭節闂堟侗鍎忛柦鍐枛閺屻劌鈹戦崱鈺傂ч梺鍝勬噺閻擄繝寮诲☉妯锋闁告鍋為悘宥夋⒑閸︻厼鍘村ù婊冪埣楠炲啫螖閸愨晛鏋傞梺鍛婃处閸撴盯藝閵娾晜鈷戠紓浣股戦幆鍫㈢磼缂佹绠為柣娑卞櫍瀹曟﹢濡告惔銏☆棃鐎规洏鍔戦、娆撴嚍閵壯冪闂傚倷鑳堕、濠囧磻閹邦喗鍋橀柕澶嗘櫅缁€鍫熺節闂堟侗鍎愰柛濠傚閳ь剙绠嶉崕閬嵥囨导鏉戠厱闁瑰濮风壕钘壝归敐鍫濅簵闁瑰濮抽悞濠冦亜閹惧崬鐏柣鎾存礀閳规垿鎮╅幓鎺嗗亾閸︻厽瀚婚柨鐔哄У閻撴瑦顨ラ悙鑼虎闁诲繆鏅犻弻宥囨喆閸曨偆浼岄悗瑙勬礀閻栧ジ宕洪敓鐘茬妞ゅ繐鎷嬪鎾绘⒒閸屾艾鈧兘鎳楅崼鏇椻偓锕傚醇閵夈儱鐝樺銈嗗笒閸婃悂宕瑰┑鍫氬亾閸忓浜鹃梺鍛婃磵閺備線宕戦幘璇茬＜闁绘劘寮撶槐鍫曟⒑閸涘﹥纾搁柛鏂跨Ч瀵剟鍩€椤掑嫭鈷掑ù锝呮憸閺嬪啯淇婇銏狀仼閾荤偞淇婇妶鍕厡妞も晛寮剁换婵嬫濞戝崬鍓遍梺缁樻尪閸庣敻寮婚敓鐘茬倞妞ゎ厼顑愭禍顏堝箖濮椻偓瀹曪絾寰勫Ο娲诲晬闂備胶绮崝鏍亹閸愵喖姹叉繛鍡樻尰閻撶喖鏌ㄥ┑鍡欑缂佲檧鍋撴俊銈囧Х閸嬫稓绮旇ぐ鎺戠鐟滅増甯╅弫鍐煏閸繂鈧憡绂嶉幆褉鏀介柣妯虹－椤ｅ弶銇勯妷銉敾闁靛洤瀚伴獮鍥煛娴ｈ桨鐥┑鐘灱濞夋稓鈧矮鍗冲濠氭偄鏉炴壆鍓ㄩ梺鍝勭Р閸斿秹鎮甸弴銏＄厽閹兼番鍔嶅☉褔鏌曢崼鐔稿€愮€规洘妞介崺鈧い鎺嶉檷娴滄粓鏌熼悜妯虹仴妞ゅ浚浜弻宥夋煥鐎ｎ亞鐟ㄩ梺闈涙鐢帡锝炲┑瀣櫜闁告侗鍓欓ˉ姘攽閻樺灚鏆╅柛瀣耿瀹曠娀鎮╃拠鑼槯闂佺粯鍔﹂崜娑㈠煡婢舵劖鎳氶柡宥庡幗閻撴洘绻涢幋婵嗚埞闁哄鍠愮换娑㈠川椤撶喎鏋犲┑顔硷攻濡炰粙鐛幇顓熷劅闁冲灈鏅滅€氫粙姊绘担渚劸妞ゆ垵妫濋獮鎴﹀炊椤掍焦娅?00闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕偓浠嬫晸閵夆晛纾婚柕蹇嬪€栭悡鏇㈡煟閹邦垰鐨洪柛鈺嬬稻閹便劍绻濋崘鈹夸虎濠碘槅鍋勯崯顐﹀煡婢跺缍囬柕濞垮灪閻忎線姊婚崒娆戭槮闁硅姤绮嶉幈銊╂偨閹肩偐鍋撻崘鈺冪瘈闁稿被鍊曞▓?
            int estimatedSize=1000+(p.c2s.size()+p.s2c.size())*300;
            StringBuilder sb=new StringBuilder(estimatedSize);
            sb.append("package ").append(boPkg).append(";\n\n");
            sb.append("import io.netty.channel.Channel;\n");
            sb.append("import ").append(protoPkg).append(".ProtoIds;\n");
            sb.append("import ").append(protoPkg).append(".*;\n");
            sb.append("import java.util.*;\n");
            sb.append("import ").append(JavaRuntimeSupport.bytesPackage(protoPkg)).append(".*;\n");
            sb.append("import ").append(JavaRuntimeSupport.serializePackage(protoPkg)).append(".*;\n");
            sb.append("import ").append(JavaRuntimeSupport.protoPackage(protoPkg)).append(".PayloadBuilder;\n");
            sb.append("public interface I").append(base).append("BO {\n");
            for(Method m: p.c2s){
                sb.append("    void ").append(m.name).append("(Channel channel");
                for(Field f: m.params){
                    sb.append(", ").append(mapType(f.type)).append(" ").append(f.name);
                }
                sb.append(");\n");
            }
            for(Method m: p.s2c){
                List<Field> presenceFields=presenceFields(m.params);
                sb.append("    default void ").append(m.name).append("(Channel channel");
                for(Field f: m.params){
                    sb.append(", ").append(mapType(f.type)).append(" ").append(f.name);
                }
                sb.append("){\n");
                sb.append("        // Use the pooled linear buffer path for payload building.\n");
                sb.append("        int __expectedSize=ByteIO.sizeOfPresenceBits(").append(presenceFields.size()).append(");\n");
                sb.append("        int __size=__expectedSize;\n");
                for(Field f: m.params){
                    if(isPresenceTrackedType(f.type)){
                        sb.append("        if(").append(javaHasWireValueExpr(f.name, f)).append("){\n");
                        appendJavaEstimateFieldStatements(sb, f.name, f, "            ", false);
                        sb.append("        }\n");
                    }else{
                        appendJavaEstimateFieldStatements(sb, f.name, f, "        ", false);
                    }
                }
                sb.append("        __expectedSize=__size;\n");
                sb.append("        PayloadBuilder.sendSized(channel, ProtoIds.").append(base.toUpperCase()).append("_").append(m.name.toUpperCase()).append(", __expectedSize, output -> {\n");
                appendJavaPresenceWritePrelude(sb, presenceFields, "", "output", "            ", "ByteIO");
                for(Field f: m.params){
                    if(isOptionalType(f.type)){
                        sb.append("            if(").append(optionalPresentExpr(f.name)).append("){ ")
                                .append(writeCursorValue("output", f.name+".get()", genericBody(f.type).trim())).append("; }\n");
                    }else if(isPresenceTrackedType(f.type)){
                        sb.append("            if(").append(javaHasWireValueExpr(f.name, f.type)).append("){ ")
                                .append(writeCursorValue("output", f.name, f.type)).append("; }\n");
                    }else{
                        sb.append("            ").append(writeCursorValue("output", f.name, f.type)).append(";\n");
                    }
                }
                sb.append("        });\n");
                sb.append("    }\n");
            }
            sb.append("}\n");
            return sb.toString();
        }
        static void appendJavaReadValueToLocal(StringBuilder sb, String localName, String t, String bufVar, String indent, boolean hot){
            sb.append(indent).append(mapType(t)).append(" ").append(localName);
            if(isStructType(t) && isInlineStructType(t)){
                sb.append("=null");
            }
            sb.append(";\n");
            appendJavaAssignReadValue(sb, localName, t, bufVar, indent, hot);
        }
        static void appendJavaReadValueToLocal(StringBuilder sb, String localName, Field f, String bufVar, String indent, boolean hot){
            sb.append(indent).append(mapType(f)).append(" ").append(localName);
            if(!fieldHasMetadataDrivenCodec(f) && isStructType(f.type) && isInlineStructType(f.type)){
                sb.append("=null");
            }
            sb.append(";\n");
            appendJavaAssignReadValue(sb, localName, f, bufVar, indent, hot);
        }
        static void appendJavaAssignReadValue(StringBuilder sb, String targetExpr, Field f, String bufVar, String indent, boolean hot){
            if(fieldHasMetadataDrivenCodec(f)){
                sb.append(indent).append(targetExpr).append("=").append(readCursorValue(bufVar, f)).append(";\n");
                return;
            }
            appendJavaAssignReadValue(sb, targetExpr, f.type, bufVar, indent, hot);
        }
        static void appendJavaAssignReadValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean hot){
            if(isStructType(t) && isInlineStructType(t)){
                appendJavaInlineReadStructValue(sb, targetExpr, structDef(t), bufVar, indent, hot);
                return;
            }
            if(hot && isHotExpandedType(t)){
                appendJavaHotReadValue(sb, targetExpr, t, bufVar, indent, true);
                return;
            }
            sb.append(indent).append(targetExpr).append("=").append(readCursorValue(bufVar, t)).append(";\n");
        }
        static boolean isJavaReusableObjectType(String t){
            return !isPrimitive(t)
                    && !t.equals("String")
                    && !ENUMS.contains(t)
                    && !t.equals("Integer")
                    && !t.equals("Long")
                    && !t.equals("Byte")
                    && !t.equals("Short")
                    && !t.equals("Boolean")
                    && !t.equals("Character")
                    && !t.equals("Float")
                    && !t.equals("Double")
                    && !isContainerType(t)
                    && !t.endsWith("[]");
        }
        static boolean isJavaHotTailSkipStruct(String t){
            Struct nested=structDef(t);
            return nested!=null && nested.hot;
        }
        static boolean isHotReusableMapKeyType(String t){
            return t.equals("int")
                    || t.equals("Integer")
                    || t.equals("long")
                    || t.equals("Long")
                    || t.equals("String");
        }
        static boolean isJavaReusableReadTargetType(String t){
            return t.endsWith("[]")
                    || isListLikeType(t)
                    || isSetLikeType(t)
                    || isQueueLikeType(t)
                    || isMapLikeType(t)
                    || isJavaReusableObjectType(t);
        }
        static void appendJavaReadIntoMethodBody(StringBuilder sb, Struct s, List<Field> presenceFields, String bufType, String bufVar){
            boolean hotMode=s.hot;
            sb.append("    public static void readInto(").append(bufType).append(" ").append(bufVar).append(", ").append(s.name).append(" o){\n");
            sb.append("        if(o==null) throw new NullPointerException(\"target object can not be null\");\n");
            appendJavaPresenceReadPrelude(sb, presenceFields.size(), bufVar, "        ", "ByteIO");
            if(useJavaFullPresenceFastPath(presenceFields.size())){
                sb.append("        if(__presence==").append(javaFullPresenceMaskLiteral(presenceFields.size())).append("){\n");
                appendJavaReadAllPresentFields(sb, s, "o.", bufVar, "            ", true, hotMode);
                sb.append("            return;\n");
                sb.append("        }\n");
            }
            if(useJavaDominantMaskFamilies(presenceFields, hotMode)){
                List<Integer> dominantCounts=dominantMaskPresentCounts(presenceFields);
                for(int i=1;i<dominantCounts.size();i++){
                    int presentCount=dominantCounts.get(i);
                    sb.append("        if(__presence==").append(javaPresencePrefixMaskLiteral(presentCount)).append("){\n");
                    appendJavaReadDominantMaskFields(sb, s, "o.", bufVar, "            ", true, hotMode, presentCount);
                    sb.append("            return;\n");
                    sb.append("        }\n");
                }
            }
            int presenceIndex=0;
            for(Field f: s.fields){
                String fieldExpr="o."+f.name;
                if(isPresenceTrackedType(f.type)){
                    String presentExpr=javaPresenceExpr("__presence", presenceIndex++, presenceFields.size());
                    if(isOptionalType(f.type)){
                        String inner=genericBody(f.type).trim();
                        sb.append("        if(").append(presentExpr).append("){\n");
                        if(isJavaReusableReadTargetType(inner)){
                            String valueVar=childVar(fieldExpr, "value");
                            sb.append("            ").append(mapType(inner)).append(" ").append(valueVar).append("=")
                                    .append(optionalPresentExpr(fieldExpr)).append(" ? ").append(fieldExpr).append(".get() : null;\n");
                            appendJavaAssignReadExistingValue(sb, valueVar, inner, bufVar, "            ", hotMode);
                            sb.append("            ").append(fieldExpr).append("=Optional.ofNullable(").append(valueVar).append(");\n");
                        }else{
                            appendJavaReadValueToLocal(sb, "__value", inner, bufVar, "            ", hotMode);
                            sb.append("            ").append(fieldExpr).append("=Optional.ofNullable(__value);\n");
                        }
                        sb.append("        }else{\n");
                        sb.append("            ").append(fieldExpr).append("=Optional.empty();\n");
                        sb.append("        }\n");
                    }else{
                        sb.append("        if(").append(presentExpr).append("){\n");
                        appendJavaAssignReadExistingValue(sb, fieldExpr, f, bufVar, "            ", hotMode);
                        sb.append("        }else{\n");
                        appendJavaResetReadValue(sb, fieldExpr, f, "            ");
                        sb.append("        }\n");
                    }
                }else{
                    appendJavaAssignReadExistingValue(sb, fieldExpr, f, bufVar, "        ", hotMode);
                }
            }
            sb.append("    }\n");
        }
        static void appendJavaAssignReadExistingValue(StringBuilder sb, String targetExpr, Field f, String bufVar, String indent, boolean hot){
            if(isPackedPrimitiveListField(f)){
                appendJavaReadExistingPackedListValue(sb, targetExpr, f, bufVar, indent);
                return;
            }
            if(isPackedPrimitiveMapField(f) || isPackedIntKeyObjectMapField(f)){
                appendJavaReadExistingPackedMapValue(sb, targetExpr, f, bufVar, indent, hot);
                return;
            }
            if(fieldHasMetadataDrivenCodec(f)){
                sb.append(indent).append(targetExpr).append("=").append(readCursorValue(bufVar, f)).append(";\n");
                return;
            }
            appendJavaAssignReadExistingValue(sb, targetExpr, f.type, bufVar, indent, hot);
        }
        static void appendJavaAssignReadExistingValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean hot){
            if(isStructType(t) && isInlineStructType(t)){
                appendJavaInlineReadStructValue(sb, targetExpr, structDef(t), bufVar, indent, hot);
                return;
            }
            if(isOptionalType(t)){
                sb.append(indent).append(targetExpr).append("=").append(readCursorValue(bufVar, t)).append(";\n");
                return;
            }
            if(isJavaReusableObjectType(t)){
                String reuseVar=childVar(targetExpr, "reuse");
                sb.append(indent).append(mapType(t)).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                sb.append(indent).append("if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("    ").append(reuseVar).append("=new ").append(mapType(t)).append("();\n");
                sb.append(indent).append("    ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("}\n");
                sb.append(indent).append(t).append(".readInto(").append(bufVar).append(", ").append(reuseVar).append(");\n");
                return;
            }
            if(t.endsWith("[]")){
                appendJavaReadExistingArrayValue(sb, targetExpr, t, bufVar, indent, hot);
                return;
            }
            if(isListLikeType(t)){
                appendJavaReadExistingListValue(sb, targetExpr, t, bufVar, indent, hot);
                return;
            }
            if(isSetLikeType(t)){
                appendJavaReadExistingSetValue(sb, targetExpr, t, bufVar, indent, hot);
                return;
            }
            if(isQueueLikeType(t)){
                appendJavaReadExistingQueueValue(sb, targetExpr, t, bufVar, indent, hot);
                return;
            }
            if(isMapLikeType(t)){
                appendJavaReadExistingMapValue(sb, targetExpr, t, bufVar, indent, hot);
                return;
            }
            sb.append(indent).append(targetExpr).append("=").append(readCursorValue(bufVar, t)).append(";\n");
        }
        static void appendJavaResetReadValue(StringBuilder sb, String targetExpr, Field f, String indent){
            if(fieldHasMetadataDrivenJavaType(f) || isFixedLengthStringField(f) || isFixedCountArrayField(f)){
                sb.append(indent).append(targetExpr).append("=").append(javaDefaultValueExpr(f)).append(";\n");
                return;
            }
            appendJavaResetReadValue(sb, targetExpr, f.type, indent);
        }
        static void appendJavaResetReadValue(StringBuilder sb, String targetExpr, String t, String indent){
            if(t.endsWith("[]")){
                sb.append(indent).append(targetExpr).append("=").append(javaDefaultValueExpr(t)).append(";\n");
                return;
            }
            if(isListLikeType(t) || isSetLikeType(t) || isQueueLikeType(t) || isMapLikeType(t)){
                String reuseVar=childVar(targetExpr, "reuse");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    ").append(mapType(t)).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                sb.append(indent).append("    if(").append(reuseVar).append("!=null){\n");
                sb.append(indent).append("        ").append(javaRecycleCollectionStmt(reuseVar, t)).append(";\n");
                sb.append(indent).append("        ").append(targetExpr).append("=").append(javaDefaultValueExpr(t)).append(";\n");
                sb.append(indent).append("    }else{\n");
                sb.append(indent).append("        ").append(targetExpr).append("=").append(javaDefaultValueExpr(t)).append(";\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("}\n");
                return;
            }
            sb.append(indent).append(targetExpr).append("=").append(javaDefaultValueExpr(t)).append(";\n");
        }
        static void appendJavaReadExistingArrayValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean hot){
            String inner=t.substring(0, t.length()-2).trim();
            // SIMD闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；婵炴垟鎳為崶顒佸仺缂佸瀵ч悗顒勬倵楠炲灝鍔氭い锔诲灣缁牏鈧綆鍋佹禍婊堟煙閺夊灝顣抽柟顔笺偢閺屽秷顧侀柛鎾寸缁绘稒绻濋崶褏鐣哄┑掳鍊曢幊鎰暤娓氣偓閺屾盯鈥﹂幋婵囩亪婵犳鍠栨鎼佲€旈崘顔嘉ч煫鍥ㄦ尵濡诧綁姊洪幖鐐插婵炲鐩幃楣冩偪椤栨ü姹楅梺鍦劋閸ㄥ綊鏁嶅鍫熲拺缂備焦锚婵洦銇勯弴銊ュ籍鐎规洏鍨介弻鍡楊吋閸℃ぞ鐢绘繝鐢靛Т閿曘倝宕幘顔肩煑闁告洦鍨遍悡蹇涙煕閳╁喚娈旈柡鍡悼閳ь剝顫夊ú蹇涘礉鎼淬劌鐒垫い鎺嶈兌閳洟鎳ｉ妶澶嬬厵闁汇値鍨奸崵娆愩亜椤忓嫬鏆ｅ┑鈥崇埣瀹曞崬鈻庤箛锝嗘缂傚倸鍊风粈渚€顢栭崱娑樼闁告挆鍐ㄧ亰婵犵數濮甸懝鍓х矆閸垺鍠愬鑸靛姇绾惧鏌熼崜褏甯涢柛瀣剁節閺屸剝寰勭€ｉ潧鍔屽┑鈽嗗亜閻倸顫忓ú顏勪紶闁靛鍎涢敐鍡欑闁告瑥顦遍惌鎺楁煙瀹曞洤浠遍柡灞芥椤撳ジ宕卞Δ渚囧悑闂傚倷绶氬褔鎮ч崱妞曟椽濡搁埡鍌涙珫濠电姴锕ら悧濠囧煕閹达附鈷戞い鎰╁€曟禒婊堟煠濞茶鐏￠柡鍛埣椤㈡岸鍩€椤掑嫬钃熼柨婵嗩槹閺呮煡鏌涢埄鍐噮闁汇倕瀚伴幃妤冩喆閸曨剛顦梺鍝ュУ閻楃娀濡存担鑲濇棃宕ㄩ鐙呯床婵犳鍠楅敃鈺呭礈濞戙埄鏁婇柛銉墯閳锋帒霉閿濆洨鎽傞柛銈嗙懃铻栭柣妯哄级閹插摜绱掗娆惧殭妞ゆ挸鍚嬪鍕節閸曞墎鍚归梻浣告惈椤︻垶鎮ч崘顔肩柧婵犲﹤鍟伴弳锕傛煛鐏炶鍔滈柣鎾寸懇閺岀喎鐣￠幏灞筋伃闂佺粯甯婄划娆撳蓟瀹ュ牜妾ㄩ梺鍛婃尰閻熲晠鐛繝鍌ゆ建闁逞屽墴婵″瓨鎷呴懖婵囨瀹曘劑顢橀悪鈧Σ瑙勪繆閻愵亜鈧牜鏁幒妤€纾归柟闂磋兌瀹撲線鏌涢鐘插姕闁抽攱甯掗湁闁挎繂鐗婇鐘绘偨椤栨稓鈯曢柕鍥у椤㈡﹢鎮欓弶鎴炵亷婵＄偑鍊戦崹娲€冩繝鍌ゅ殨濠电姵鑹惧敮闂佹寧娲嶉崑鎾寸箾閸繄鐒告慨濠呮缁棃宕卞Δ鈧瀛樼箾閸喐绀嬮柡宀嬬秮楠炴鈧潧鎲￠崚娑㈡⒑鐠団€虫灍闁挎洏鍨介獮鍐ㄢ枎閹惧磭顔岄梺鐟版惈濡瑧鈧灚鐗楃换婵嬫偨闂堟稐娌悷婊勬緲閸熸挳銆佸棰濇晣闁靛繒濮烽崝锕€顪冮妶鍡楃瑐缂佽绻濆畷顖濈疀濞戞瑧鍘遍梺缁橆焾濞呮洜绮堥崼銉︾厱闁圭儤鎸哥粭鎺楁煃鐠囨煡鍙勬鐐叉喘椤㈡棃宕卞▎鎴炴瘞婵犵數濮甸鏍窗濡ゅ啯宕查柛宀€鍋為崕妤呮煕椤愶絾鍎曢柨婵嗘处鐎氭氨鈧懓澹婇崰妤冣偓闈涚焸濮婃椽妫冨☉姘暫闂佸摜鍣ラ崑濠傜暦濠靛棭娼╂い鎾寸矆缁ㄥ姊洪幐搴㈢；婵＄偞甯″畷銉╊敃閿濆嫮绠氶梺鍛婄懃椤︻垱鎱ㄥ鍡╂闁绘劖鎯岄悞浠嬫煃瑜滈崜姘辩矙閹烘鏅俊鐐€曠€涒晠骞愰崜褎顫曢柟鎯х摠婵挳鏌涢幘鏉戠祷闁告捇娼ч埞鎴︽倷瀹割喗效濠电偛寮剁划搴ㄥ礆閹烘绫嶉柛顐亝閺咁亪姊洪柅鐐茶嫰婢ь喗銇勯銏㈢閻撱倖銇勮箛鎾愁仼缂佹劖绋掔换婵嬫偨闂堟刀銏ゆ倵濞戞帗娅婇柟顕€绠栧畷濂稿即閻斿弶瀚奸梻浣告啞缁哄潡宕曢柆宥呭嚑閹兼番鍨荤粻鍓х棯椤撱埄妫戠紒鈾€鍋撴俊銈囧Х閸嬫盯宕幘顔惧祦闁糕剝鍑瑰Σ楣冩⒑閸濆嫭顥滄い鎴濐樀瀵鎮㈤悡搴ｎ槰闁荤姴娉ч崟顒佹瘞濠电姷顣槐鏇㈠磻濡厧鍨濇繛鍡樻尭杩濇繛鎾村焹閸嬫挾鈧鍣崳锝呯暦閻撳簶鏀介柛鈩冪懅瀹曞搫鈹戦敍鍕杭闁稿﹥鐗曢～蹇旂節濮橆剙鍋嶉悷婊冮叄楠炲牓濡搁埡浣侯槰濡炪倖妫佽闁归绮换娑欐綇閸撗呅氬┑鈽嗗亜鐎氭澘鐣烽妷鈺傚仭闁逛絻娅曢弬鈧俊鐐€栧Λ浣规叏閵堝洨绀婇柟杈鹃檮閸嬪倿鏌曢崼婵愭Ч闁绘挾鍠愭穱濠囶敍濞戝崬鍔岄梺鎼炲€栭悷褏妲愰幒妤€鐒垫い鎺戝闁卞洭鏌ｉ弮鍥仩闁伙箑鐗撳濠氬磼濮樺崬顤€婵炴挻纰嶉〃濠傜暦閺囷紕鐤€婵炴垶鐟ч崢鎼佹煟鎼搭垳宀涢柡鍛箘缁綁寮崼鐔哄幐闁诲繒鍋熼崑鎾剁矆鐎ｎ兘鍋撶憴鍕闁告鍥х厴闁硅揪绠戦悙濠勬喐韫囨稒鍋橀柍鍝勫暟绾捐棄霉閿濆懏鎯堝ù婊冨⒔閳ь剝顫夊ú姗€鏁嬮柧鑽ゅ仱閺屾盯寮撮妸銉т哗閺夆晜绻堝娲捶椤撶偛濡洪梺绯曟櫅閻楀棝鈥﹂崶顒€鐓涢柛灞久肩花濠氭⒑閻熺増鎯堢紒澶婄埣瀹曟繂顓兼径瀣幍濡炪倖姊婚弲顐﹀箠閸曨厾纾肩紓浣诡焽閵嗘帡鏌嶈閸撴氨绮欓幒妞烩偓锕傚炊椤掍礁鍓归梺闈涚墕椤︿即鎮￠弴銏＄厪濠电偟鍋撳▍鍐煙閸欏鍔ら柍瑙勫灴閺佸秹宕熼浣圭槗闁诲氦顫夊ú婊堝窗閺嶎厹鈧礁鈽夊鍡樺兊濡炪倖宸婚崑鎾剁磼娓氬洤娅嶆慨濠勭帛閹峰懘鎼归獮搴撳亾婵犲洦鐓曢柟鎯ь嚟缁犵偤鏌曢崱妤€鏆ｇ€规洖宕灒闁惧繐婀遍悰顕€姊绘担鍛婂暈婵炶绠撳畷褰掓焼瀹ュ棗浜楅梺鍝勬川閸嬫劙寮ㄦ禒瀣厽婵☆垵顕х徊缁樸亜韫囷絼閭柡宀嬬節瀹曢亶鍩℃担绯曟嫬闂備礁鎼径鍥焵椤掆偓绾绢參寮抽崱娑欏€甸柨婵嗛婢ф壆鎮敂鎴掔箚闁靛牆娲ゅ暩闂佺顑嗛惄顖炪€侀弽銊ョ窞闁归偊鍓氶悗顒勬⒑閸︻厼顣兼繝銏★耿閻涱噣濮€閵堝棛鍘撻梺鍛婄箓鐎氼剚绂嶉悙缈犵箚妞ゆ劑鍨归顓熸叏婵犲偆鐓肩€规洘甯掗埢搴ㄥ箣椤撶啘婊堟⒒娴ｅ憡璐￠柍宄扮墦瀹曟垶绻濋崶銉㈠亾娓氣偓瀵粙顢橀悙娈挎Ч婵＄偑鍊栭幐楣冨闯閵夆晛纾婚柕濞垮劗閺€鑺ャ亜閺冨倻鎽傞柣鎺斿亾缁绘稒寰勭€ｎ剚鍒涢梺褰掓敱閸ㄥ潡骞冮姀銈呯闁兼祴鏅涢獮妤呮⒒娴ｅ憡鎯堥柛濠呮閳绘棃寮撮悙鈺傜亖闂備緡鍓欑粔鐢告偂濞戙垺鍊甸柨婵嗛娴滄繃銇勯妷銉уⅵ闁哄矉绱曟禒锕傚礈瑜庨崚娑㈡⒑閻愯棄鍔电紒鐘虫崌閻涱喚鈧綆浜栭弨浠嬫煕閵夈垺娅冮柟鏌ョ畺濮婂宕掑▎鎰偘濡炪倖娉﹂崨顔煎簥闂佺鎻粻鎴犵不閺屻儲鐓曢柕澶嬪灥閺堫剛绱炴惔銊︹拺闁告稑锕︾粻鎾斥攽閻愯韬柟鐓庢贡閹叉挳宕熼銈呴叡闂傚倷绀佸﹢杈╁垝椤栨粍鏆滈柣鎰閺佸﹪鏌熼柇锕€骞掔紒璇叉閺岋綁骞囬崗鍝ョ泿闂侀€炲苯澧柣妤冨Т閻ｇ柉銇愰幒鎾充缓闂佸搫顦Λ妤€煤椤撱垹绠栭柣锝呯灱閻瑩鎮归幁鎺戝闁哄棭鍋婂缁樻媴閼恒儳銆婇梺鍝ュУ閹稿宕氭繝鍥舵晣闁靛繒濮甸悗顒勬⒑閸涘﹤濮﹂柛鐘愁殜閹繝濡烽埡鍌滃幐闂佹悶鍎洪悡鍫濃枔閺傛５鐟邦煥閸垻鏆┑顔硷龚濞咃絿妲愰幒鎳崇喖宕崟鍨秼闂傚倷鐒︾€笛兠洪敂鐣岊洸妞ゅ繐鐗忓畵渚€鏌涢幇鍏哥凹闁哥姴妫濋弻娑㈠即濡や焦鐝旀繛瀛樼矆閸楀啿顫忓ú顏勬嵍妞ゆ挆鍛Ъ缂傚倷闄嶉崝宥咁渻娴犲鏄ラ柍褜鍓氶妵鍕箳閸℃ぞ澹曢梻浣告憸閸犳劙骞愰崘宸殨閻犲洦绁村Σ鍫ユ煏韫囧ň鍋撻崗鍛潖闂備浇宕垫慨鏉戔枖瑜斿畷鍗炍旀担绋垮濠电姷鏁告慨鐢割敊閺嶎厼绐楅柡宥庡幗閺呮繈鏌曟径鍫濆姶婵炴捁顕ч湁闁绘ê妯婇崕蹇涙煢閸愵亜鏋涢柡灞剧☉閳藉螣閸忓吋鍠栭梻浣侯焾閿曪箓宕楀鈧濠氬Χ婢跺娈為梺缁橆殔閻楀繘濡撮幇顒夋富闁靛牆鎳忕粋瀣煕濡吋娅曢柟骞垮灩閳规垹鈧綆浜為崢閬嶆⒑闁偛鑻晶瀵糕偓瑙勬礃鐢帡锝炲┑瀣垫晢闁稿本锚娴犲繘姊婚崒姘偓椋庣矆娓氣偓楠炴牠顢曢敃鈧€氬銇勯幒鎴濐仾闁稿骸瀛╅妵鍕冀椤愵澀绮剁紓浣哄У閻楃娀寮诲澶婁紶闁告洦鍓欏▍锝囩磽娴ｆ彃浜鹃梺鍛婂姦閸犳鎮″▎鎴犵＝濞达絽顫栭鍛攳妞ゆ牗绋撶粻楣冩煕韫囨挸鎮戠紒鈧€ｎ偅鍙忓┑鐘插鐢稓绱掑Δ鍐ㄦ灈闁糕斁鍋撳銈嗗笒鐎氼剟鎮為崹顐犱簻闁圭儤鍨甸埀顒€鎲＄粋鎺戭煥閸喓鍘惧┑鐐跺蔼椤曆囨倶閿熺姵鐓涢柛娑卞幘閸╋絾銇勯姀锛勬创闁诡喗鐟ч埀顒傛暩椤牓鐛鈧娲嚒閵堝懏鐎鹃梺鑽ゅ枂閸庢娊鍩€椤掍礁鍤柛鎾跺枎閻ｅ嘲鈹戠€ｎ€冾熆鐠轰警鍎戦柛娆忔濮婃椽宕崟顒€顦╅梺鎸庡哺閺岋綀绠涢弮鍌氬绩闂佸搫鐭夌槐鏇熺閿曞倹鍤嶉柕澶樺枟椤ワ絾淇婇悙顏勨偓鏍箰閹间礁围缂佸娉曢弳锕傛煏婵炵偓娅撻柡浣哥Ч閺屻劌鈹戦崱妯烘闂佸搫妫岄弲鐘差潖婵犳艾纾兼慨姗嗗厴閸嬫挸鐣￠幊濠傜秺瀹曟儼顦撮柡鍡畵閺岋綁濮€閵忊晝鍔哥紓浣瑰姈椤ㄥ﹪寮婚悢鍏肩劷闁挎洍鍋撴鐐达耿閺岋絽鈹戦崱娆忕厽闂佽鍠楅〃濠囧极閹邦厽鍎熼柍鈺佸暟娴滎亞绱撻崒姘偓椋庢媼閺屻儱鐤炬繛鎴欏灪缁犳帡姊绘担铏瑰笡闁挎碍淇婇姘捐含鐎规洘娲熼獮鍥偋閸垹骞楅梻浣筋潐閸庢娊顢氶銏犵疇闁搞儺鍓氶悡娑氣偓鍏夊亾闁逞屽墴瀹曚即寮介婧惧亾娴ｇ硶妲堥柕蹇曞Т閼板灝鈹戞幊閸婃劙宕戦幘缁樼厸閻庯絻鍔岄埀顒佺箞瀵鏁撻悩鎻掕€块梺褰掑亰閸撴稒绂掗悡搴富闁靛牆绨肩花濠氭煕閻旈鎽犲ǎ鍥э躬楠炲棝骞嶉鐐緫婵犳鍠楅敃鈺呭礈濮樿泛纾归柡鍥ュ灪閳锋帒霉閿濆懏鍟為柟顖氱墦閺岀喖宕橀懠顒傤唺闂佸憡甯楃敮妤呭箚閺冨牆惟闁靛／灞芥暭闂傚倷绶氬褏鎹㈤幒鎾村弿妞ゆ挾鍊ｉ敐澶婇唶闁绘梻顭堝鍨攽椤旂瓔娈旈柣妤€绻樺畷姘跺级濡數鎳撻…銊╁醇閵忋垺姣囬梻浣告惈閻绱炴担瑙勫弿闁逞屽墴閺屽秹鎸婃径瀣垫闂佸搫鎳樻禍璺侯潖閾忓湱纾兼俊顖濇閻熴劍绻濋埛鈧崒婊呯厯闂佺硶鏂侀崑鎾愁渻閵堝棗绗傞柤鍐茬埣閸╁﹪寮撮悩鐢碉紲闁荤姴娲╃亸娆愭櫠閺囩喆浜滄い鎰╁灮缁犱即鎮￠妶鍡愪簻闊洦鎸婚崳鐟懊归悪鈧崣鍐潖濞差亜浼犻柛鏇炵仛鏁堥梺璇叉捣閻熸娊宕橀崘鈺佹诞妤犵偛娲、妯衡攽閸垻鏆伴梻鍌欑窔濞佳囨偋閸℃娲Χ閸℃瑥鈪伴梻鍌氬€峰ù鍥敋閺嶎厼绐楅柡宥庡幖绾惧綊鏌涢…鎴濇珮闁搞倖娲橀妵鍕箛閸撲胶鏆犵紓浣插亾闁告劏鏂傛禍婊堟煛閸愩劌鈧敻骞忛埄鍐闁绘挸鍑介煬顒佹叏婵犲啯銇濇俊顐㈠暙閳藉顫濋澶嬫瘒闂備浇顕х€涒晝绮欓崼銉ョ柧闁绘ê妯婇崵鏇㈡煙缂佹ê淇柣鎾卞劦閺屾盯顢曢妶鍛瘣闂佸綊鏀卞钘夘潖濞差亜宸濆┑鐘插濡插牓姊洪幐搴㈢８闁稿骸纾崚鎺楀醇閳垛晛浜鹃柨婵嗛娴滅偟绱掗悩鍐插姢闂囧鏌ㄥ┑鍡樺櫣闁哄棝浜堕弻娑㈡偄閸濆嫧鏋呴悗娈垮枟閹倸顕ｉ鈧畷濂告偄閸濆嫬濡囨繝鐢靛Х閺佹悂宕戦悢鐓庣；闁圭偓鏋奸弸宥夋煕閳╁啰鈯曢柣鎾冲暣濮婃椽宕归鍛壈闂佽绻愰悺銊╁箞閵婏妇绡€闁告劏鏂傛禒銏狀渻閵堝啫鐏繛鑼枑娣囧﹪宕奸弴鐐茬獩濡炪倖甯掗崐鐟扮暦閻斿吋鈷掗柛灞剧懅椤︼箓鏌涘顒夊剰妞ゎ亜鍟村畷绋课旈埀顒勫垂閸岀偞鈷戞い鎺嗗亾缂佸鎸抽幃鎸庛偅閸愨晝鍘遍梺瑙勫礃鐏忣亪顢欐径鎰厱閻庯綆鍋呭畷宀勬煙椤旇崵鐭欓柟顔荤矙閹瑩宕楁径濠佸婵犵數濮村ú锕傛偂閺囥垺鍊甸柨婵嗛娴滄繈鎮樿箛鏇熸毈闁哄瞼鍠栧畷锝嗗緞鐎ｎ亜鏀柣搴ゎ潐濞叉粓宕伴弽顓溾偓浣肝旈崨顓狅紲闂佹寧姊归崕鎶藉几閻樼粯鈷掗柛灞捐壘閳ь剛鍏橀幊妤呭醇閺囨せ鍋撻敃鍌氶唶婵犮垺绻傜紞濠囧箖濠婂牊瀵犲璺侯儑閳ь剦鍓氱换婵嬫偨闂堟刀锝嗘叏濡濡奸柣锝呯仛缁楃喖鍩€椤掑嫬钃熸繛鎴欏灩缁犳盯姊婚崼鐔衡姇闁诲繐鐗撳缁樻媴閺傘倝鐛滈梺绋匡攻閻楁绮氭潏銊х瘈闁搞儺鐏涜閺屾盯寮撮妸銉ュ濠碘€虫▕閸犳鎹㈠☉姘ｅ亾閻㈡鐒炬い搴＄焸閺屾盯濡搁妸銈呮儓闂佺懓绠嶉崹褰掑煘閹寸姭鍋撻敐搴′航婵☆偄鍟埞鎴︽倷閺夋垹浼囨俊鐐存綑閹芥粓鎮疯缁绘繄鍠婃径宀€锛熼梺绋款儐閸ㄥ灝鐣烽幇鏉垮嵆闁靛繆鈧厖鍑介梻浣虹帛閹稿摜鈧灚甯掗…鍥冀瑜夐弨浠嬫煟濡櫣鏋冨瑙勵焽閻ヮ亪骞嗚閹垹绱掔紒妯兼创鐎规洖宕灒闁惧繒鎳撴慨鍏肩節绾版ǚ鍋撻搹顐熸灆闂侀潻缍囩徊浠嬶綖韫囨拋娲敂閸曨剙绁舵俊鐐€栭幐鑽ょ矙閹烘柡鍋撳顓犳创婵﹨娅ｇ划娆忊枎閹冨闂備焦瀵уú蹇涘磹濠靛绠栧Δ锝呭暞閸婅崵绱掑☉姗嗗剱闁哄拋鍓熷铏圭磼濡搫顫戦柣蹇撶箲閻熲晠銆佸▎鎾冲窛妞ゆ牗绮庨敍婵囩箾鏉堝墽鍒版繝鈧柆宥呯叀濠㈣泛顑冩禍婊堟煙鐎涙绠栨い銉ｅ灮閳ь剚顔栭崰妤呮偂閿熺姴鏄ラ柨鐔哄Т瀹告繃銇勯弮鍥舵綈閻庢艾銈搁弻锝夋偄閸濄儳鐓傛繝鈷€鍕垫畼闁轰緡鍣ｉ獮鎺楀箻妫版繃閿ゅ┑鐐差嚟閸樠囨偤閵娾晜鍋傞柡鍥ュ灪閻撶喐淇婇婵愬殭濠⒀屽灡缁绘盯宕奸悢铏圭厜闂?
            if(SIMD_ENABLED && hot && isPrimitive(inner)){
                if(inner.equals("byte")){
                    appendJavaSIMDReadExistingByteArray(sb, targetExpr, t, bufVar, indent);
                    return;
                }else if(inner.equals("int")){
                    appendJavaSIMDReadExistingIntArray(sb, targetExpr, t, bufVar, indent);
                    return;
                }else if(inner.equals("long")){
                    appendJavaSIMDReadExistingLongArray(sb, targetExpr, t, bufVar, indent);
                    return;
                }else if(inner.equals("float")){
                    appendJavaSIMDReadExistingFloatArray(sb, targetExpr, t, bufVar, indent);
                    return;
                }else if(inner.equals("double")){
                    appendJavaSIMDReadExistingDoubleArray(sb, targetExpr, t, bufVar, indent);
                    return;
                }
            }
            String arrayType=mapType(t);
            String countVar=childVar(targetExpr, "count");
            String reuseVar=childVar(targetExpr, "reuse");
            String indexVar=childVar(targetExpr, "index");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    ").append(arrayType).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("    if(").append(countVar).append("==0){\n");
            sb.append(indent).append("        ").append(targetExpr).append("=").append(javaDefaultValueExpr(t)).append(";\n");
            sb.append(indent).append("    }else{\n");
            sb.append(indent).append("        if(").append(reuseVar).append("==null || ").append(reuseVar).append(".length!=").append(countVar).append("){\n");
            sb.append(indent).append("            ").append(reuseVar).append("=").append(javaArrayAllocationExpr(t, countVar)).append(";\n");
            sb.append(indent).append("            ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("        }\n");
            sb.append(indent).append("        for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            appendJavaAssignReadExistingValue(sb, reuseVar+"["+indexVar+"]", inner, bufVar, indent+"            ", hot);
            sb.append(indent).append("        }\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaSIMDReadExistingByteArray(StringBuilder sb, String targetExpr, String t, String bufVar, String indent){
            String countVar=childVar(targetExpr, "count");
            String reuseVar=childVar(targetExpr, "reuse");
            String arrayType=mapType(t);
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    ").append(arrayType).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("    if(").append(countVar).append("==0){\n");
            sb.append(indent).append("        ").append(targetExpr).append("=new byte[0];\n");
            sb.append(indent).append("    }else{\n");
            sb.append(indent).append("        if(").append(reuseVar).append("==null || ").append(reuseVar).append(".length!=").append(countVar).append("){\n");
            sb.append(indent).append("            ").append(reuseVar).append("=new byte[").append(countVar).append("];\n");
            sb.append(indent).append("            ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("        }\n");
            sb.append(indent).append("        ByteIO.readRawByteArray(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(");\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaSIMDReadExistingIntArray(StringBuilder sb, String targetExpr, String t, String bufVar, String indent){
            String countVar=childVar(targetExpr, "count");
            String reuseVar=childVar(targetExpr, "reuse");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    int[] ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("    if(").append(countVar).append("==0){\n");
            sb.append(indent).append("        ").append(targetExpr).append("=new int[0];\n");
            sb.append(indent).append("    }else{\n");
            sb.append(indent).append("        if(").append(reuseVar).append("==null || ").append(reuseVar).append(".length!=").append(countVar).append("){\n");
            sb.append(indent).append("            ").append(reuseVar).append("=new int[").append(countVar).append("];\n");
            sb.append(indent).append("            ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("        }\n");
            sb.append(indent).append("        ByteIO.readRawIntArray(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(");\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaSIMDReadExistingLongArray(StringBuilder sb, String targetExpr, String t, String bufVar, String indent){
            String countVar=childVar(targetExpr, "count");
            String reuseVar=childVar(targetExpr, "reuse");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    long[] ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("    if(").append(countVar).append("==0){\n");
            sb.append(indent).append("        ").append(targetExpr).append("=new long[0];\n");
            sb.append(indent).append("    }else{\n");
            sb.append(indent).append("        if(").append(reuseVar).append("==null || ").append(reuseVar).append(".length!=").append(countVar).append("){\n");
            sb.append(indent).append("            ").append(reuseVar).append("=new long[").append(countVar).append("];\n");
            sb.append(indent).append("            ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("        }\n");
            sb.append(indent).append("        ByteIO.readRawLongArray(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(");\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaSIMDReadExistingFloatArray(StringBuilder sb, String targetExpr, String t, String bufVar, String indent){
            String countVar=childVar(targetExpr, "count");
            String reuseVar=childVar(targetExpr, "reuse");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    float[] ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("    if(").append(countVar).append("==0){\n");
            sb.append(indent).append("        ").append(targetExpr).append("=new float[0];\n");
            sb.append(indent).append("    }else{\n");
            sb.append(indent).append("        if(").append(reuseVar).append("==null || ").append(reuseVar).append(".length!=").append(countVar).append("){\n");
            sb.append(indent).append("            ").append(reuseVar).append("=new float[").append(countVar).append("];\n");
            sb.append(indent).append("            ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("        }\n");
            sb.append(indent).append("        ByteIO.readRawFloatArray(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(");\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaSIMDReadExistingDoubleArray(StringBuilder sb, String targetExpr, String t, String bufVar, String indent){
            String countVar=childVar(targetExpr, "count");
            String reuseVar=childVar(targetExpr, "reuse");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    double[] ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("    if(").append(countVar).append("==0){\n");
            sb.append(indent).append("        ").append(targetExpr).append("=new double[0];\n");
            sb.append(indent).append("    }else{\n");
            sb.append(indent).append("        if(").append(reuseVar).append("==null || ").append(reuseVar).append(".length!=").append(countVar).append("){\n");
            sb.append(indent).append("            ").append(reuseVar).append("=new double[").append(countVar).append("];\n");
            sb.append(indent).append("            ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("        }\n");
            sb.append(indent).append("        ByteIO.readRawDoubleArray(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(");\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaReadExistingListValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean hot){
            String inner=genericBody(t).trim();
            String canonical=canonicalContainerType(t);
            String listType=mapType(t);
            String countVar=childVar(targetExpr, "count");
            String reuseVar=childVar(targetExpr, "reuse");
            String existingCountVar=childVar(targetExpr, "existingCount");
            String indexVar=childVar(targetExpr, "index");
            String elemVar=childVar(targetExpr, "elem");
            if(isSpecializedIntListType(t)){
                sb.append(indent).append("{\n");
                sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
                sb.append(indent).append("    IntArrayList ").append(reuseVar).append("=(").append(targetExpr).append(" instanceof IntArrayList) ? (IntArrayList)").append(targetExpr).append(" : ByteIO.borrowIntArrayList(").append(countVar).append(");\n");
                sb.append(indent).append("    ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("    ").append(reuseVar).append(".ensureCapacity(").append(countVar).append(");\n");
                sb.append(indent).append("    int ").append(existingCountVar).append("=").append(reuseVar).append(".size();\n");
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                sb.append(indent).append("        int ").append(elemVar).append("=ByteIO.readInt(").append(bufVar).append(");\n");
                sb.append(indent).append("        if(").append(indexVar).append("<").append(existingCountVar).append("){\n");
                sb.append(indent).append("            ").append(reuseVar).append(".setInt(").append(indexVar).append(", ").append(elemVar).append(");\n");
                sb.append(indent).append("        }else{\n");
                sb.append(indent).append("            ").append(reuseVar).append(".addInt(").append(elemVar).append(");\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("    if(").append(existingCountVar).append(">").append(countVar).append("){ ").append(reuseVar).append(".truncate(").append(countVar).append("); }\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isSpecializedLongListType(t)){
                sb.append(indent).append("{\n");
                sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
                sb.append(indent).append("    LongArrayList ").append(reuseVar).append("=(").append(targetExpr).append(" instanceof LongArrayList) ? (LongArrayList)").append(targetExpr).append(" : ByteIO.borrowLongArrayList(").append(countVar).append(");\n");
                sb.append(indent).append("    ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("    ").append(reuseVar).append(".ensureCapacity(").append(countVar).append(");\n");
                sb.append(indent).append("    int ").append(existingCountVar).append("=").append(reuseVar).append(".size();\n");
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                sb.append(indent).append("        long ").append(elemVar).append("=ByteIO.readLong(").append(bufVar).append(");\n");
                sb.append(indent).append("        if(").append(indexVar).append("<").append(existingCountVar).append("){\n");
                sb.append(indent).append("            ").append(reuseVar).append(".setLong(").append(indexVar).append(", ").append(elemVar).append(");\n");
                sb.append(indent).append("        }else{\n");
                sb.append(indent).append("            ").append(reuseVar).append(".addLong(").append(elemVar).append(");\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("    if(").append(existingCountVar).append(">").append(countVar).append("){ ").append(reuseVar).append(".truncate(").append(countVar).append("); }\n");
                sb.append(indent).append("}\n");
                return;
            }
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    ").append(listType).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            if("LinkedList".equals(canonical) || "Collection".equals(canonical)){
                sb.append(indent).append("    if(").append(reuseVar).append("==null){\n");
                if("LinkedList".equals(canonical)){
                    sb.append(indent).append("        ").append(reuseVar).append("=new LinkedList<>();\n");
                }else{
                    sb.append(indent).append("        ").append(reuseVar).append("=").append(javaBorrowCollectionExpr(t, countVar)).append(";\n");
                }
                sb.append(indent).append("        ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("    }else if(!").append(reuseVar).append(".isEmpty()){\n");
                sb.append(indent).append("        // 濠电姷鏁告慨鐑藉极閸涘﹥鍙忛柣鎴ｆ閺嬩線鏌涘☉姗堟敾闁告瑥绻橀弻锝夊箣閿濆棭妫勯梺鍝勵儎缁舵岸寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ゆい顓犲厴瀵鏁愭径濠勭杸濡炪倖甯婄拋鏌ュ几濞嗘挻鈷戠紓浣姑粭鈺佲攽椤斿搫鈧骞戦姀鐘闁靛繒濮撮懓鍨攽閳藉棗鐏ユい鏇嗗懎鏋堢€广儱顦伴悡鐔兼煟閺傛寧鎲搁柟铏礈缁辨帡鎮╅搹顐㈢３濡ょ姷鍋涢崯顐ョ亙闂佸憡渚楅崰妤€鈻嶅鍫熺厵闁兼祴鏅炶棢闂佸憡鎸荤换鍫ュ箖濡警鍚嬪璺侯儌閹锋椽姊洪崨濠勭畵閻庢凹鍘介崚濠囨偂楠炵喓鎳撻…銊︽償濠靛牏娉挎俊鐐€ら崑鍕崲濮椻偓楠炴牠宕烽鐔锋瀭闂佸憡娲﹂崑鍡氥亹閹绢喗鈷掑ù锝呮啞閹牓鎮跺鐓庝喊鐎规洘娲栫叅妞ゅ繐瀚崝锕€顪冮妶鍡楃瑐缂佸灈鈧枼鏋旀繝濠傜墛閻撴稓鈧厜鍋撻悗锝庡墰琚ｇ紓鍌欒兌婵敻鎯勯姘煎殨妞ゆ帒瀚崹鍌涖亜閺冨洤袚闁搞倕鐗撳濠氬磼濞嗘劗銈板銈嗘礃閻楃姴鐣烽幎绛嬫晬婵犲﹤瀚惔濠傗攽閻樼粯娑фい鎴濇嚇瀵憡绗熼埀顒勫蓟閻旂厧绀堢憸蹇曟暜濞戙垺鐓曢悗锝庡亜婵秹鏌＄仦鍓р槈闁宠姘︾粻娑㈡晲閸犺埇鍔戝娲焻閻愯尪瀚板褍顭烽弻娑㈠箻鐠虹儤鐏堝Δ鐘靛仜閸燁偉鐏冮梺鍛婁緱閸ㄦ壆绮婇敃鍌涒拺闁告捁灏欓崢娑㈡煕閻樺磭澧柟渚垮妽閹棃濡搁敂瑙勫闂備浇宕甸崰鎰熆濡綍锝囩磼濡晲绨婚梺鐟扮摠缁诲啴宕甸崶顒佺厓闁靛鍨抽悾鐢碘偓瑙勬礀閻栧ジ銆佸Δ鍛劦妞ゆ帒瀚崑鍌涚箾閹存瑥鐏柣鎾存礋閹﹢鎮欓幍顔炬毌闂佸吋绁撮弲鐐存叏閸愭祴鏀介柣妯虹－椤ｆ煡鏌嶉柨瀣伌闁哄本鐩弫鍌滄嫚閹绘帞顔愰梻浣告啞閺屻劑顢栨径鎰摕婵炴垯鍨规儫閻熸粌閰ｅ鎶筋敆閸曨剛鍘遍梺瀹狀潐閸庤櫕绂嶉悙顑跨箚闁绘劦浜滈埀顒佺墱閺侇噣骞掑Δ鈧壕褰掓煠婵劕鈧牠寮冲鍕闁瑰瓨鐟ラ悘顏堟煟閹捐泛鏋涢柡灞界Ч婵＄兘濡疯缁辩偟绱撴担鍝勑ョ紒顕呭灦楠炲牓濡搁敂鍓х槇闂佺琚崐妤呭触鐎ｎ喗鍊甸悷娆忓缁€鍐偨椤栨稑娴柛鈹垮灪閹棃濡搁妷褜鍞洪梻浣烘嚀閻°劎鎹㈤崒鐐村殘闁归偊鍠氱壕浠嬫煕鐏炴崘澹橀柍褜鍓熼ˉ鎾斥枎閵忋倖鏅搁柣妯荤叀濡嘲顪冮妶鍡樼５闁稿鎸鹃埀顒冾潐濞叉牜绱炴繝鍥╁祦閹兼番鍔嶇€电姴顭跨捄铏圭劸婵″弶鎸冲缁樻媴閸涘﹨纭€闂佺绨洪崐婵嬬嵁閹邦厾绡€婵﹩鍓涢敍鐔兼⒑缂佹ɑ鐓ラ柛姘儔閹ょ疀濞戞艾褰勯梺鎼炲劀瀹ュ懍绱ｅ┑鐘媰閸曨厼寮ㄩ梺鍝勭焿缁蹭粙鍩為幋锕€骞㈡俊銈勭悼閳哄懏鈷戦悗鍦濞兼劙鏌涢妸銉у煟闁绘侗鍠楃换婵嬪炊閵娧冨箞濠电姷鏁告慨鎾疮椤愶絾鍙忕€广儱娲犻崑鎾舵喆閸曨剛顦ㄩ梺鎼炲妼濞硷繝鎮伴鍢夌喖鎳栭埡鍐跨床婵犵妲呴崹鎶藉储瑜嶉锝夊箮閼恒儮鎷绘繛杈剧到閹诧繝骞嗛崼鐔虹閻犲泧鍛殼闂佽桨绀侀澶婎潖閾忚瀚氶柛娆忣槺椤╃増绻涚€涙鐭岄柛瀣枎鍗遍柟鐗堟緲缁犳娊鏌熺€圭姵鐝俊顐㈠暙閻ｅ嘲顫滈埀顒佷繆閹间焦鏅滈柛娆嶅劦閸嬫姊婚崒姘偓鐑芥嚄閸撲焦鍏滈柛顐ｆ礀濡ê銆掑锝呬壕閻庤娲橀崹鍧楃嵁濮椻偓楠炲洦鎷呴悷鎵В闂傚倷绶氬褔鎮ч崱妞㈡稑螖閸涱參鏁滄繝鐢靛Т濞诧箓鎮￠弴銏″€甸柨婵嗛娴滄繈鎮樿箛搴″祮闁哄本娲熷畷鎯邦槻妞ゅ浚鍘鹃埀顒侇問閸犳骞愰幎濮愨偓渚€寮崼婵嗚€垮┑鐐叉噽閸庛倗鍒掑畝鍕ㄢ偓鏃堝礃椤斿槈褔鏌涢幇鈺佸濞寸姵鍎抽埞鎴︽倷鐠鸿櫣姣㈤梺鍝ュУ閻楁洟顢氶敐澶樻晝闁挎洍鍋撶痪鎯с偢閺岋綁骞囬鐔虹▏闂佽皫鍐仾缂佺粯绻堟慨鈧柨婵嗘閵嗘劕顪冮妶蹇曠暢濞存粠鍓涘Σ鎰版倷瀹割喖鎮戞繝銏ｆ硾椤戝倿骞忔繝姘拺缂佸瀵у﹢浼存煟閻旀繂娉氶崶顒佹櫇闁逞屽墴閳ワ箓宕稿Δ浣镐画闁汇埄鍨奸崰娑㈠触椤愶附鍊甸悷娆忓缁€鍐煕閵娿儳浠㈤柣锝囧厴婵℃悂鍩℃繝鍐╂珫婵犵數鍋為崹鍫曟晪缂備降鍔婇崕闈涱潖缂佹ɑ濯撮柛娑橈工閺嗗牆鈹戦悙棰濆殝缂佺姵鎸搁悾鐤亹閹烘垹楠囬梺鍦焾濞寸兘宕欐禒瀣拻闁稿本鑹鹃埀顒傚厴閹偤鏁傞柨顖氫壕缂佹绋戦崢鎯洪鍕敤濡炪倖鎸鹃崑鐔兼晬濞嗘挻鍋℃繝濠傛噹椤ｅジ鎮介娑樻诞闁诡噯绻濆鎾閿涘嫬骞嶉梻浣虹帛閸ㄥ爼鏁嬪┑鐐茬墱閸犳岸骞夐幖浣瑰亱闁割偅绻勯悷鏌ユ⒑閸濆嫭鍣洪柣鎿勭節瀵鈽夐姀鐘栥劑鏌ㄥ┑鍡樺櫣妞ゎ剙鐗嗛埞鎴︽倷閼碱剙顣洪梺娲诲墲閸嬫劕危閹版澘绠婚悗娑櫭鎾绘⒑閼恒儍顏埶囬幎钘夋瀬闁秆勵殕閸婄敻鎮峰▎蹇擃仾缂佲偓閸儲鐓涘ù锝勮閻掗箖宕￠柆宥嗙厵闁绘鐗婄欢鑼磼閻樺啿鈻曢柡宀嬬節瀹曟﹢濡搁妷銏犱壕濠电姵纰嶉弲顒佺節婵犲倸鏆婇柡鈧懞銉ｄ簻闁哄啫鍊婚幗鍌涚箾閸喐鈷愬ǎ鍥э躬椤㈡洟鏁愰崶銊ユ珰闂備浇顕栭崰姘跺磻閹惧墎绱﹀ù鐘差儏瀹告繂鈹戦悙闈涗壕閻庢艾銈稿缁樻媴閽樺鎯為梺鍝ュУ閸旀牜绮╅悢鐓庡耿婵炴垶鐟ユ禒鐓庮渻閵堝棙灏柛銊︽そ閹繝寮撮姀锛勫帾婵犵數鍋涢悘婵嬪礉閵堝洨纾煎璺侯儐閵囨繈鏌＄仦鍓ф创闁糕晝鍋ゅ畷褰掝敊閸欍儳妫梻鍌欐祰椤曟牠宕归崡鐐嶆盯宕橀埡鈧换鍡涙煙闂傚顦﹂崬顖炴偡濠婂啰绠荤€殿喖鐖煎畷銊︾節娴ｈ櫣鐣鹃梻浣虹帛閸旓附绂嶅鍫濈劦妞ゆ帊鑳舵晶閬嶆煛娓氬洤娅嶆鐐村笒铻栭柍褜鍓涙竟鏇㈡偡閹佃櫕鏂€闂佺粯锚绾绢參銆傞弻銉︾厱閻庯綆鍊栭幋锕€桅闁告洦鍨伴崡铏繆閵堝倸浜炬繛瀛樼矒缁犳牕顫忓ú顏勪紶闁告洦鍘鹃崝鍦磽閸屾氨小缂佽埖宀搁悰顕€宕橀纰辨綂闂侀潧鐗嗛幊宥囨閸洘鈷戦梻鍫熷喕缁憋繝鏌涘☉鍗炲箹缂佹鐭傚濠氬磼濮橆兘鍋撻悜鑺ュ€块柨鏇楀亾妞ゎ亜鍟村畷绋课旈埀顒傜矆閸喓绠鹃柛鈩兩戠亸顓㈠炊閹绢喗鈷戠憸鐗堝笚閿涚喓绱掗埀顒佹媴閾忛€涚瑝闂佺粯顭囩划顖炴偂閻斿吋鐓欓梺顓ㄧ畱閺嬫盯鏌涢弬娆惧剶闁哄矉缍侀獮鎺楀即閻愮増鐫忛梻渚€鈧偛鑻晶顔剧磼閻樿尙效鐎规洘娲樺蹇涘煘閹傚濠电偞鍨剁敮妤€鈻嶆繝鍕ㄥ亾濞堝灝鏋熼柣鎾墲閹便劑鍩€椤掑嫭鐓忛柛顐ｇ箖椤ユ垿鏌熼柨瀣仢婵﹥妞藉畷銊︾節閸曘劍顫嶉梻浣瑰濞插繘宕曢幓鎺濆殫闁告洦鍨扮粈瀣亜閺嶃劎銆掗柛姗€浜堕弻锝嗘償椤栨粎校婵炲瓨绮嶇换鍫濈暦閻㈢鐐婃い鎺嶇閻у嫰姊虹紒妯荤叆闁硅姤绮庡褔鍩€椤掑嫭鈷掗柛灞炬皑婢ф稑銆掑顓ф疁鐎规洘娲熼獮鍥偋閸垹甯楅梻鍌欑閻忔繈顢栭崨瀛樺€堕柍鍝勫暟绾惧ジ寮堕崼娑樺閻忓繋鍗抽弻鐔风暋閻楀牆娈楅梺璇″灠閸婂灝顫忚ぐ鎺戠疀妞ゆ棁娉曠粻鍙夌節閻㈤潧啸闁轰焦鎮傚畷鎴濃槈閵忊€充罕婵犵數濮存导锝呪槈閵忕姷顦ㄥ銈嗘婢瑰牓骞楅弴銏♀拺闁圭娴风粻鎾绘煙閸欏鑰跨€殿噮鍣ｅ畷鐓庘攽閸繂袝濠碉紕鍋戦崐鏍暜閹烘柡鍋撳鐓庡籍鐎殿喗褰冮埞鎴犫偓锝庡亞閸橆亪姊洪崜鎻掍簼缂佽绉村嵄闁归棿鐒﹂悡鐔兼煙閹屽殶缂佺姷鍋ら弻鈥崇暆閳ь剟宕伴弽顓犲祦闁硅揪绠戠粻娑㈡⒒閸喓鈯曟い鏂垮濮婂宕掑▎鎴М閻庤娲﹂崜娆戝弲濡炪倖鎸堕崹褰掓嫅閻斿吋鐓ラ柣鏂挎惈瀛濋梺缁樺姇閿曨亪寮诲鍡樺闁规鍠栨俊浠嬫⒑鐠囪尙绠ｇ紒鑸佃壘椤繐煤椤忓嫬绐涙繝鐢靛Т濞寸兘宕濋悜鑺モ拺閺夌偞澹嗛ˇ锔姐亜椤撶偛妲绘い鏇稻缁绘繂顫濋鐔哥彸濠电姰鍨煎▔娑㈩敄閸涱喚顩茬憸鐗堝笚閳锋垿鏌涘☉姗堝姛闁瑰啿鍟妵鍕晝閸屾瑦鍠氶柦妯荤箞濮婃椽顢楅埀顒傜矓閻㈢鐓曢柟杈鹃檮閳锋垶銇勯幇鈺佲偓鏇熺濠婂牊鐓犳繛鑼额嚙閻忥繝鏌￠崨顓犲煟鐎规洩绲惧鍕暆閳ь剟顢欓弴銏♀拺闁告稑锕ｇ欢閬嶆煕閻樻剚娈樼紒顔肩墢閳ь剨缍嗛崰妤呮偂濞嗘劑浜滈柡鍥殔娴滄儳鈹戦悙鏉垮皟闁搞儜鍛箣闂備胶鎳撻顓熸叏椤撱垹纾婚柟鍓х帛閺呮煡骞栫划鍏夊亾閼碱剛鍩嶉梻鍌欑缂嶅﹪寮ㄩ崡鐑嗘富闁芥ê顦藉鏍р攽閻樺疇澹樼痪鎯у悑閹便劌螣閸濆嫯鍩炲銈忕悼閺佽顫忓ú顏勪紶闁告洦鍓欏▍銈囩磽娴ｇ瓔鍤欓悗姘煎櫍瀹曟岸骞掗幋鏃€鐎婚梺鐟邦嚟婵兘鏁嶅鍐ｆ斀闁宠棄妫楅悘鐘崇節閳ь剚娼忛埡鍐х瑝濠殿喗顭堥崺鏍煕閹寸姷纾藉ù锝咁潠椤忓懏鍙忕€光偓閸曨剛鍘遍梺鍐叉惈閸婄粯鏅堕悽鍛婂癄闁绘柨顨庨悢鍡涙偣鏉炴媽顒熼柛搴＄Т闇夋繝濠傚閻帡鏌″畝瀣瘈鐎规洖鐖煎鐢告偨閸偅娅︾紓鍌氬€风粈渚€藝娴犲绐楁俊銈呮噹妗呴梺鍛婃处閸ㄤ即锝為崨瀛樼厓闁靛鍎遍弳閬嶆煙閻ｅ本鏆慨濠呮閸栨牠寮撮悢鍛婄番闂備胶顭堥鍡涘箰閹间讲鈧棃宕橀鍢壯囨煕閳╁喚娈橀柣鐔村姂閺岋絾鎯旈姀鐘叉瘓闂佸憡鎸荤粙鎾诲礆閹烘挾绡€婵﹩鍘兼禍褰掓倵鐟欏嫭绀€婵炲眰鍔戦幆渚€宕奸悢铏圭槇闂佹眹鍨藉褍鐡梻浣侯焾閿曪妇鍒掗鐐茬柧閻犻缚銆€濡插牊绻涢崱妤冃＄紒銊ヮ煼濮婃椽妫冨☉杈ㄐら梺绋挎唉濞呮洟寮茬捄琛℃婵浜敍婊呯磽閸屾瑧鍔嶆い顓炴搐鐓ら悗娑欙供濞堜粙鏌ｉ幇顒佲枙闁稿孩妫冮弻鈩冩媴閻熸澘顫嶉悗鍨緲鐎氼厾鎹㈠┑瀣闁宠桨鐒﹂悾濂告⒒閸屾艾鈧悂宕愬畡鎳婂綊宕堕澶嬫櫔閻熸粌绉瑰﹢浣逛繆閻愬樊鍎忛悗娑掓櫊瀵啿顭ㄩ崼鐔哄幗濠殿喗顭堟ご鎼佀夐　顪竆n");
                sb.append(indent).append("        ").append(reuseVar).append(".clear();\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendJavaReadValueToLocal(sb, elemVar, inner, bufVar, indent+"        ", hot);
                sb.append(indent).append("        ").append(reuseVar).append(".add(").append(elemVar).append(");\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("}\n");
                return;
            }
            sb.append(indent).append("    if(").append(reuseVar).append("==null){\n");
            sb.append(indent).append("        ").append(reuseVar).append("=").append(javaBorrowCollectionExpr(t, countVar)).append(";\n");
            sb.append(indent).append("        ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("    if(").append(reuseVar).append(" instanceof ArrayList){\n");
            sb.append(indent).append("        ((ArrayList<").append(mapType(inner)).append(">)").append(reuseVar).append(").ensureCapacity(").append(countVar).append(");\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("    int ").append(existingCountVar).append("=").append(reuseVar).append(".size();\n");
            sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            sb.append(indent).append("        if(").append(indexVar).append("<").append(existingCountVar).append("){\n");
            if(isJavaReusableReadTargetType(inner)){
                sb.append(indent).append("            ").append(mapType(inner)).append(" ").append(elemVar).append("=").append(reuseVar).append(".get(").append(indexVar).append(");\n");
                appendJavaAssignReadExistingValue(sb, elemVar, inner, bufVar, indent+"            ", hot);
                sb.append(indent).append("            ").append(reuseVar).append(".set(").append(indexVar).append(", ").append(elemVar).append(");\n");
            }else{
                appendJavaReadValueToLocal(sb, elemVar, inner, bufVar, indent+"            ", hot);
                sb.append(indent).append("            ").append(reuseVar).append(".set(").append(indexVar).append(", ").append(elemVar).append(");\n");
            }
            sb.append(indent).append("        }else{\n");
            appendJavaReadValueToLocal(sb, elemVar, inner, bufVar, indent+"            ", hot);
            sb.append(indent).append("            ").append(reuseVar).append(".add(").append(elemVar).append(");\n");
            sb.append(indent).append("        }\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("    if(").append(existingCountVar).append(">").append(countVar).append("){\n");
            sb.append(indent).append("        ").append(reuseVar).append(".subList(").append(countVar).append(", ").append(existingCountVar).append(").clear();\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaReadExistingPackedListValue(StringBuilder sb, String targetExpr, Field f, String bufVar, String indent){
            String t=f.type;
            String inner=genericBody(t).trim();
            String canonical=canonicalContainerType(t);
            String listType=mapType(t);
            String countVar=childVar(targetExpr, "count");
            String reuseVar=childVar(targetExpr, "reuse");
            String existingCountVar=childVar(targetExpr, "existingCount");
            String indexVar=childVar(targetExpr, "index");
            if("LinkedList".equals(canonical) || "Collection".equals(canonical) || !(isIntLikeType(inner) || isLongLikeType(inner))){
                sb.append(indent).append(targetExpr).append("=").append(readCursorValue(bufVar, f)).append(";\n");
                return;
            }
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    ").append(listType).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            if(isIntLikeType(inner) && !"ArrayList".equals(canonical)){
                String packedVar=childVar(targetExpr, "packed");
                sb.append(indent).append("    if(").append(reuseVar).append(" instanceof IntArrayList){\n");
                sb.append(indent).append("        IntArrayList ").append(packedVar).append("=(IntArrayList)").append(reuseVar).append(";\n");
                sb.append(indent).append("        ").append(packedVar).append(".resize(").append(countVar).append(");\n");
                sb.append(indent).append("        ByteIO.readRawIntArray(").append(bufVar).append(", ").append(packedVar).append(".rawArray(), ").append(countVar).append(");\n");
                sb.append(indent).append("        ").append(targetExpr).append("=").append(packedVar).append(";\n");
                sb.append(indent).append("    }else{\n");
                sb.append(indent).append("        if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("            ").append(reuseVar).append("=ByteIO.borrowArrayList(").append(countVar).append(");\n");
                sb.append(indent).append("            ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("        if(").append(reuseVar).append(" instanceof ArrayList){\n");
                sb.append(indent).append("            ((ArrayList<Integer>)").append(reuseVar).append(").ensureCapacity(").append(countVar).append(");\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("        int ").append(existingCountVar).append("=").append(reuseVar).append(".size();\n");
                sb.append(indent).append("        for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                sb.append(indent).append("            Integer __value=ByteIO.readFixedInt(").append(bufVar).append(");\n");
                sb.append(indent).append("            if(").append(indexVar).append("<").append(existingCountVar).append("){\n");
                sb.append(indent).append("                ").append(reuseVar).append(".set(").append(indexVar).append(", __value);\n");
                sb.append(indent).append("            }else{\n");
                sb.append(indent).append("                ").append(reuseVar).append(".add(__value);\n");
                sb.append(indent).append("            }\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("        if(").append(existingCountVar).append(">").append(countVar).append("){\n");
                sb.append(indent).append("            ").append(reuseVar).append(".subList(").append(countVar).append(", ").append(existingCountVar).append(").clear();\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("    }\n");
            }else if(isLongLikeType(inner) && !"ArrayList".equals(canonical)){
                String packedVar=childVar(targetExpr, "packed");
                sb.append(indent).append("    if(").append(reuseVar).append(" instanceof LongArrayList){\n");
                sb.append(indent).append("        LongArrayList ").append(packedVar).append("=(LongArrayList)").append(reuseVar).append(";\n");
                sb.append(indent).append("        ").append(packedVar).append(".resize(").append(countVar).append(");\n");
                sb.append(indent).append("        ByteIO.readRawLongArray(").append(bufVar).append(", ").append(packedVar).append(".rawArray(), ").append(countVar).append(");\n");
                sb.append(indent).append("        ").append(targetExpr).append("=").append(packedVar).append(";\n");
                sb.append(indent).append("    }else{\n");
                sb.append(indent).append("        if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("            ").append(reuseVar).append("=ByteIO.borrowArrayList(").append(countVar).append(");\n");
                sb.append(indent).append("            ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("        if(").append(reuseVar).append(" instanceof ArrayList){\n");
                sb.append(indent).append("            ((ArrayList<Long>)").append(reuseVar).append(").ensureCapacity(").append(countVar).append(");\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("        int ").append(existingCountVar).append("=").append(reuseVar).append(".size();\n");
                sb.append(indent).append("        for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                sb.append(indent).append("            Long __value=ByteIO.readFixedLong(").append(bufVar).append(");\n");
                sb.append(indent).append("            if(").append(indexVar).append("<").append(existingCountVar).append("){\n");
                sb.append(indent).append("                ").append(reuseVar).append(".set(").append(indexVar).append(", __value);\n");
                sb.append(indent).append("            }else{\n");
                sb.append(indent).append("                ").append(reuseVar).append(".add(__value);\n");
                sb.append(indent).append("            }\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("        if(").append(existingCountVar).append(">").append(countVar).append("){\n");
                sb.append(indent).append("            ").append(reuseVar).append(".subList(").append(countVar).append(", ").append(existingCountVar).append(").clear();\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("    }\n");
            }else{
                sb.append(indent).append("    ").append(targetExpr).append("=").append(readCursorValue(bufVar, f)).append(";\n");
            }
            sb.append(indent).append("}\n");
        }
        static void appendJavaReadProjectedPackedListValue(StringBuilder sb, String targetExpr, Field f, String bufVar, String indent, String limitExpr){
            String t=f.type;
            String inner=genericBody(t).trim();
            String canonical=canonicalContainerType(t);
            String listType=mapType(t);
            String countVar=childVar(targetExpr, "count");
            String limitVar=childVar(targetExpr, "limit");
            String readCountVar=childVar(targetExpr, "readCount");
            String reuseVar=childVar(targetExpr, "reuse");
            String existingCountVar=childVar(targetExpr, "existingCount");
            String indexVar=childVar(targetExpr, "index");
            int scalarBytes=javaFixedSkipScalarBytes(inner);
            if("LinkedList".equals(canonical) || "Collection".equals(canonical) || scalarBytes<=0 || !(isIntLikeType(inner) || isLongLikeType(inner))){
                appendJavaReadProjectedListValue(sb, targetExpr, t, bufVar, indent, false, limitExpr);
                return;
            }
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    int ").append(limitVar).append("=").append(limitExpr).append(";\n");
            sb.append(indent).append("    int ").append(readCountVar).append("=").append(limitVar).append("<0 ? ").append(countVar)
                    .append(" : Math.min(").append(countVar).append(", Math.max(").append(limitVar).append(", 0));\n");
            sb.append(indent).append("    ").append(listType).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            if(isIntLikeType(inner) && !"ArrayList".equals(canonical)){
                String packedVar=childVar(targetExpr, "packed");
                sb.append(indent).append("    if(").append(reuseVar).append(" instanceof IntArrayList){\n");
                sb.append(indent).append("        IntArrayList ").append(packedVar).append("=(IntArrayList)").append(reuseVar).append(";\n");
                sb.append(indent).append("        ").append(packedVar).append(".resize(").append(readCountVar).append(");\n");
                sb.append(indent).append("        ByteIO.readRawIntArray(").append(bufVar).append(", ").append(packedVar).append(".rawArray(), ").append(readCountVar).append(");\n");
                sb.append(indent).append("        ").append(targetExpr).append("=").append(packedVar).append(";\n");
                sb.append(indent).append("    }else{\n");
                sb.append(indent).append("        if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("            ").append(reuseVar).append("=ByteIO.borrowArrayList(").append(readCountVar).append(");\n");
                sb.append(indent).append("            ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("        if(").append(reuseVar).append(" instanceof ArrayList){\n");
                sb.append(indent).append("            ((ArrayList<Integer>)").append(reuseVar).append(").ensureCapacity(").append(readCountVar).append(");\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("        int ").append(existingCountVar).append("=").append(reuseVar).append(".size();\n");
                sb.append(indent).append("        for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(readCountVar).append(";").append(indexVar).append("++){\n");
                sb.append(indent).append("            Integer __value=ByteIO.readFixedInt(").append(bufVar).append(");\n");
                sb.append(indent).append("            if(").append(indexVar).append("<").append(existingCountVar).append("){\n");
                sb.append(indent).append("                ").append(reuseVar).append(".set(").append(indexVar).append(", __value);\n");
                sb.append(indent).append("            }else{\n");
                sb.append(indent).append("                ").append(reuseVar).append(".add(__value);\n");
                sb.append(indent).append("            }\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("        if(").append(existingCountVar).append(">").append(readCountVar).append("){\n");
                sb.append(indent).append("            ").append(reuseVar).append(".subList(").append(readCountVar).append(", ").append(existingCountVar).append(").clear();\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("    }\n");
            }else{
                String packedVar=childVar(targetExpr, "packed");
                sb.append(indent).append("    if(").append(reuseVar).append(" instanceof LongArrayList){\n");
                sb.append(indent).append("        LongArrayList ").append(packedVar).append("=(LongArrayList)").append(reuseVar).append(";\n");
                sb.append(indent).append("        ").append(packedVar).append(".resize(").append(readCountVar).append(");\n");
                sb.append(indent).append("        ByteIO.readRawLongArray(").append(bufVar).append(", ").append(packedVar).append(".rawArray(), ").append(readCountVar).append(");\n");
                sb.append(indent).append("        ").append(targetExpr).append("=").append(packedVar).append(";\n");
                sb.append(indent).append("    }else{\n");
                sb.append(indent).append("        if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("            ").append(reuseVar).append("=ByteIO.borrowArrayList(").append(readCountVar).append(");\n");
                sb.append(indent).append("            ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("        if(").append(reuseVar).append(" instanceof ArrayList){\n");
                sb.append(indent).append("            ((ArrayList<Long>)").append(reuseVar).append(").ensureCapacity(").append(readCountVar).append(");\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("        int ").append(existingCountVar).append("=").append(reuseVar).append(".size();\n");
                sb.append(indent).append("        for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(readCountVar).append(";").append(indexVar).append("++){\n");
                sb.append(indent).append("            Long __value=ByteIO.readFixedLong(").append(bufVar).append(");\n");
                sb.append(indent).append("            if(").append(indexVar).append("<").append(existingCountVar).append("){\n");
                sb.append(indent).append("                ").append(reuseVar).append(".set(").append(indexVar).append(", __value);\n");
                sb.append(indent).append("            }else{\n");
                sb.append(indent).append("                ").append(reuseVar).append(".add(__value);\n");
                sb.append(indent).append("            }\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("        if(").append(existingCountVar).append(">").append(readCountVar).append("){\n");
                sb.append(indent).append("            ").append(reuseVar).append(".subList(").append(readCountVar).append(", ").append(existingCountVar).append(").clear();\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("    }\n");
            }
            sb.append(indent).append("    if(").append(countVar).append(">").append(readCountVar).append(") ").append(bufVar).append(".skip((")
                    .append(countVar).append("-").append(readCountVar).append(")*").append(scalarBytes).append(");\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaReadProjectedBorrowedValue(StringBuilder sb, String targetExpr, Field f, String bufVar, String indent, String limitExpr, String indicesExpr){
            String countVar=childVar(targetExpr, "count");
            String limitVar=childVar(targetExpr, "limit");
            String readCountVar=childVar(targetExpr, "readCount");
            String indicesVar=childVar(targetExpr, "indices");
            sb.append(indent).append("{\n");
            if(f.fixedLength!=null){
                sb.append(indent).append("    int ").append(countVar).append("=").append(f.fixedLength).append(";\n");
            }else{
                sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            }
            if(indicesExpr!=null){
                sb.append(indent).append("    int[] ").append(indicesVar).append("=").append(indicesExpr).append(";\n");
                sb.append(indent).append("    if(").append(indicesVar).append("!=null){\n");
                if(isBorrowedBytesField(f)){
                    sb.append(indent).append("        ").append(targetExpr).append("=ByteIO.readSampledBorrowedBytes(").append(bufVar).append(", ").append(countVar).append(", ").append(indicesVar).append(");\n");
                }else if(isBorrowedPrimitiveArrayField(f)){
                    sb.append(indent).append("        ").append(targetExpr).append("=").append(readSampledBorrowedArrayExpr(bufVar, f.type, countVar, indicesVar)).append(";\n");
                }else{
                    throw new IllegalArgumentException("unsupported borrowed projection field: "+f.type);
                }
                sb.append(indent).append("    }else{\n");
                sb.append(indent).append("        int ").append(limitVar).append("=").append(limitExpr).append(";\n");
                sb.append(indent).append("        int ").append(readCountVar).append("=").append(limitVar).append("<0 ? ").append(countVar)
                        .append(" : Math.min(").append(countVar).append(", Math.max(").append(limitVar).append(", 0));\n");
                if(isBorrowedBytesField(f)){
                    sb.append(indent).append("        ").append(targetExpr).append("=ByteIO.readBorrowedBytes(").append(bufVar).append(", ").append(countVar).append(", ").append(readCountVar).append(");\n");
                }else if(isBorrowedPrimitiveArrayField(f)){
                    sb.append(indent).append("        ").append(targetExpr).append("=").append(readBorrowedRawArrayExpr(bufVar, f.type, countVar, readCountVar)).append(";\n");
                }else{
                    throw new IllegalArgumentException("unsupported borrowed projection field: "+f.type);
                }
                sb.append(indent).append("    }\n");
            }else{
                sb.append(indent).append("    int ").append(limitVar).append("=").append(limitExpr).append(";\n");
                sb.append(indent).append("    int ").append(readCountVar).append("=").append(limitVar).append("<0 ? ").append(countVar)
                        .append(" : Math.min(").append(countVar).append(", Math.max(").append(limitVar).append(", 0));\n");
                if(isBorrowedBytesField(f)){
                    sb.append(indent).append("    ").append(targetExpr).append("=ByteIO.readBorrowedBytes(").append(bufVar).append(", ").append(countVar).append(", ").append(readCountVar).append(");\n");
                }else if(isBorrowedPrimitiveArrayField(f)){
                    sb.append(indent).append("    ").append(targetExpr).append("=").append(readBorrowedRawArrayExpr(bufVar, f.type, countVar, readCountVar)).append(";\n");
                }else{
                    throw new IllegalArgumentException("unsupported borrowed projection field: "+f.type);
                }
            }
            sb.append(indent).append("}\n");
        }
        static void appendJavaReadExistingPackedMapValue(StringBuilder sb, String targetExpr, Field f, String bufVar, String indent, boolean hot){
            List<String> kv=splitTopLevel(genericBody(f.type), ',');
            String keyType=kv.get(0).trim();
            String valueType=kv.get(1).trim();
            if(isPackedIntIntMapField(f)){
                String reuseVar=childVar(targetExpr, "reuse");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    IntIntHashMap ").append(reuseVar).append("=(").append(targetExpr).append(" instanceof IntIntHashMap) ? (IntIntHashMap)").append(targetExpr).append(" : null;\n");
                sb.append(indent).append("    ").append(targetExpr).append("=ByteIO.readPackedIntIntMap(").append(bufVar).append(", ").append(reuseVar).append(");\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isPackedIntLongMapField(f)){
                String reuseVar=childVar(targetExpr, "reuse");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    IntLongHashMap ").append(reuseVar).append("=(").append(targetExpr).append(" instanceof IntLongHashMap) ? (IntLongHashMap)").append(targetExpr).append(" : null;\n");
                sb.append(indent).append("    ").append(targetExpr).append("=ByteIO.readPackedIntLongMap(").append(bufVar).append(", ").append(reuseVar).append(");\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isPackedIntKeyObjectMapField(f)){
                String countVar=childVar(targetExpr, "count");
                String oldMapVar=childVar(targetExpr, "oldMap");
                String newMapVar=childVar(targetExpr, "newMap");
                String indexVar=childVar(targetExpr, "index");
                String keyVar=childVar(targetExpr, "key");
                String valueVar=childVar(targetExpr, "value");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
                sb.append(indent).append("    IntObjectHashMap<").append(mapType(valueType)).append("> ").append(oldMapVar).append("=(").append(targetExpr).append(" instanceof IntObjectHashMap) ? (IntObjectHashMap<").append(mapType(valueType)).append(">)").append(targetExpr).append(" : null;\n");
                if(isJavaReusableObjectType(valueType)){
                    sb.append(indent).append("    IntObjectHashMap<").append(mapType(valueType)).append("> ").append(newMapVar).append("=ByteIO.borrowIntObjectHashMap(").append(countVar).append(");\n");
                    sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                    sb.append(indent).append("        int ").append(keyVar).append("=ByteIO.readFixedInt(").append(bufVar).append(");\n");
                    sb.append(indent).append("        ").append(mapType(valueType)).append(" ").append(valueVar).append("=").append(oldMapVar).append("==null?null:").append(oldMapVar).append(".getInt(").append(keyVar).append(");\n");
                    appendJavaAssignReadExistingValue(sb, valueVar, valueType, bufVar, indent+"        ", hot);
                    sb.append(indent).append("        ").append(newMapVar).append(".putInt(").append(keyVar).append(", ").append(valueVar).append(");\n");
                    sb.append(indent).append("    }\n");
                    sb.append(indent).append("    if(").append(oldMapVar).append("!=null){ ByteIO.recycleIntObjectHashMap(").append(oldMapVar).append("); }\n");
                    sb.append(indent).append("    ").append(targetExpr).append("=").append(newMapVar).append(";\n");
                }else{
                    sb.append(indent).append("    IntObjectHashMap<").append(mapType(valueType)).append("> ").append(newMapVar).append("=").append(oldMapVar).append("==null ? ByteIO.borrowIntObjectHashMap(").append(countVar).append(") : ").append(oldMapVar).append(";\n");
                    sb.append(indent).append("    ").append(newMapVar).append(".clear();\n");
                    sb.append(indent).append("    ").append(newMapVar).append(".ensureCapacity(").append(countVar).append(");\n");
                    sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                    sb.append(indent).append("        int ").append(keyVar).append("=ByteIO.readFixedInt(").append(bufVar).append(");\n");
                    appendJavaReadValueToLocal(sb, valueVar, valueType, bufVar, indent+"        ", hot);
                    sb.append(indent).append("        ").append(newMapVar).append(".putInt(").append(keyVar).append(", ").append(valueVar).append(");\n");
                    sb.append(indent).append("    }\n");
                    sb.append(indent).append("    ").append(targetExpr).append("=").append(newMapVar).append(";\n");
                }
                sb.append(indent).append("}\n");
                return;
            }
            String reuseVar=childVar(targetExpr, "reuse");
            String valueBuf=childVar(bufVar, "packedValue");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    IntObjectHashMap<").append(mapType(valueType)).append("> ").append(reuseVar).append("=(").append(targetExpr).append(" instanceof IntObjectHashMap) ? (IntObjectHashMap<").append(mapType(valueType)).append(">)").append(targetExpr).append(" : null;\n");
            sb.append(indent).append("    ").append(targetExpr).append("=ByteIO.readPackedIntObjectMapFast(").append(bufVar).append(", ").append(reuseVar).append(", ").append(valueBuf).append("->").append(readFixedCursorValue(valueBuf, valueType)).append(");\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaReadExistingSetValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean hot){
            String inner=genericBody(t).trim();
            String setType=mapType(t);
            String countVar=childVar(targetExpr, "count");
            String reuseVar=childVar(targetExpr, "reuse");
            String indexVar=childVar(targetExpr, "index");
            String elemVar=childVar(targetExpr, "elem");
            String capacity=javaHashCapacityExpr(countVar);
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    ").append(setType).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("    if(").append(reuseVar).append("==null){\n");
            sb.append(indent).append("        ").append(reuseVar).append("=").append(javaBorrowCollectionExpr(t, countVar)).append(";\n");
            sb.append(indent).append("        ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("    }else if(!").append(reuseVar).append(".isEmpty()){\n");
            sb.append(indent).append("        // Clear only when the old set is much larger than the new payload.\n");
            sb.append(indent).append("        ").append(reuseVar).append(".clear();\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            appendJavaReadValueToLocal(sb, elemVar, inner, bufVar, indent+"        ", hot);
            sb.append(indent).append("        ").append(reuseVar).append(".add(").append(elemVar).append(");\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaReadExistingQueueValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean hot){
            String inner=genericBody(t).trim();
            String queueType=mapType(t);
            String countVar=childVar(targetExpr, "count");
            String reuseVar=childVar(targetExpr, "reuse");
            String indexVar=childVar(targetExpr, "index");
            String elemVar=childVar(targetExpr, "elem");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    ").append(queueType).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("    if(").append(reuseVar).append("==null){\n");
            sb.append(indent).append("        ").append(reuseVar).append("=").append(javaBorrowCollectionExpr(t, countVar)).append(";\n");
            sb.append(indent).append("        ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("    }else if(!").append(reuseVar).append(".isEmpty()){\n");
            sb.append(indent).append("        // Clear only when the old queue is much larger than the new payload.\n");
            sb.append(indent).append("        ").append(reuseVar).append(".clear();\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            appendJavaReadValueToLocal(sb, elemVar, inner, bufVar, indent+"        ", hot);
            sb.append(indent).append("        ").append(reuseVar).append(".add(").append(elemVar).append(");\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaReadExistingMapValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean hot){
            List<String> kv=splitTopLevel(genericBody(t), ',');
            String keyType=kv.get(0).trim();
            String valueType=kv.get(1).trim();
            if(isSpecializedIntObjectMapType(t)){
                String countVar=childVar(targetExpr, "count");
                String oldMapVar=childVar(targetExpr, "oldMap");
                String newMapVar=childVar(targetExpr, "newMap");
                String indexVar=childVar(targetExpr, "index");
                String keyVar=childVar(targetExpr, "key");
                String valueVar=childVar(targetExpr, "value");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
                sb.append(indent).append("    IntObjectHashMap<").append(mapType(valueType)).append("> ").append(oldMapVar).append("=(").append(targetExpr).append(" instanceof IntObjectHashMap) ? (IntObjectHashMap<").append(mapType(valueType)).append(">)").append(targetExpr).append(" : null;\n");
                if(isJavaReusableReadTargetType(valueType)){
                    sb.append(indent).append("    IntObjectHashMap<").append(mapType(valueType)).append("> ").append(newMapVar).append("=ByteIO.borrowIntObjectHashMap(").append(countVar).append(");\n");
                    sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                    sb.append(indent).append("        int ").append(keyVar).append("=ByteIO.readInt(").append(bufVar).append(");\n");
                    sb.append(indent).append("        ").append(mapType(valueType)).append(" ").append(valueVar).append("=").append(oldMapVar).append("==null?null:").append(oldMapVar).append(".getInt(").append(keyVar).append(");\n");
                    appendJavaAssignReadExistingValue(sb, valueVar, valueType, bufVar, indent+"        ", hot);
                    sb.append(indent).append("        ").append(newMapVar).append(".putInt(").append(keyVar).append(", ").append(valueVar).append(");\n");
                    sb.append(indent).append("    }\n");
                    sb.append(indent).append("    if(").append(oldMapVar).append("!=null){ ByteIO.recycleIntObjectHashMap(").append(oldMapVar).append("); }\n");
                    sb.append(indent).append("    ").append(targetExpr).append("=").append(newMapVar).append(";\n");
                }else{
                    sb.append(indent).append("    IntObjectHashMap<").append(mapType(valueType)).append("> ").append(newMapVar).append("=").append(oldMapVar).append("==null ? ByteIO.borrowIntObjectHashMap(").append(countVar).append(") : ").append(oldMapVar).append(";\n");
                    sb.append(indent).append("    ").append(newMapVar).append(".clear();\n");
                    sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                    sb.append(indent).append("        int ").append(keyVar).append("=ByteIO.readInt(").append(bufVar).append(");\n");
                    appendJavaReadValueToLocal(sb, valueVar, valueType, bufVar, indent+"        ", hot);
                    sb.append(indent).append("        ").append(newMapVar).append(".putInt(").append(keyVar).append(", ").append(valueVar).append(");\n");
                    sb.append(indent).append("    }\n");
                    sb.append(indent).append("    ").append(targetExpr).append("=").append(newMapVar).append(";\n");
                }
                sb.append(indent).append("}\n");
                return;
            }
            if(isJavaReusableReadTargetType(valueType) && isHotReusableMapKeyType(keyType)){
                appendJavaReadExistingReusableMapValue(sb, targetExpr, t, bufVar, indent, valueType, keyType, hot);
                return;
            }
            String mapType=mapType(t);
            String countVar=childVar(targetExpr, "count");
            String reuseVar=childVar(targetExpr, "reuse");
            String indexVar=childVar(targetExpr, "index");
            String keyVar=childVar(targetExpr, "key");
            String valueVar=childVar(targetExpr, "value");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    ").append(mapType).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("    if(").append(reuseVar).append("==null){\n");
            sb.append(indent).append("        ").append(reuseVar).append("=").append(javaBorrowCollectionExpr(t, countVar)).append(";\n");
            sb.append(indent).append("        ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("    }else if(!").append(reuseVar).append(".isEmpty()){\n");
            sb.append(indent).append("        // Clear only when the old map is much larger than the new payload.\n");
            sb.append(indent).append("        ").append(reuseVar).append(".clear();\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            appendJavaReadValueToLocal(sb, keyVar, keyType, bufVar, indent+"        ", hot);
            appendJavaReadValueToLocal(sb, valueVar, valueType, bufVar, indent+"        ", hot);
            sb.append(indent).append("        ").append(reuseVar).append(".put(").append(keyVar).append(", ").append(valueVar).append(");\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaReadExistingReusableMapValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, String valueType, String keyType, boolean hot){
            String mapType=mapType(t);
            String countVar=childVar(targetExpr, "count");
            String oldMapVar=childVar(targetExpr, "oldMap");
            String newMapVar=childVar(targetExpr, "newMap");
            String indexVar=childVar(targetExpr, "index");
            String keyVar=childVar(targetExpr, "key");
            String valueVar=childVar(targetExpr, "value");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    ").append(mapType).append(" ").append(oldMapVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("    if(").append(countVar).append("==0){\n");
            sb.append(indent).append("        if(").append(oldMapVar).append("==null){\n");
            sb.append(indent).append("            ").append(targetExpr).append("=").append(javaBorrowCollectionExpr(t, "0")).append(";\n");
            sb.append(indent).append("        }else{\n");
            sb.append(indent).append("            ").append(oldMapVar).append(".clear();\n");
            sb.append(indent).append("            ").append(targetExpr).append("=").append(oldMapVar).append(";\n");
            sb.append(indent).append("        }\n");
            sb.append(indent).append("    }else{\n");
            sb.append(indent).append("        ").append(mapType).append(" ").append(newMapVar).append("=").append(javaBorrowCollectionExpr(t, countVar)).append(";\n");
            sb.append(indent).append("        for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            appendJavaReadValueToLocal(sb, keyVar, keyType, bufVar, indent+"            ", hot);
            sb.append(indent).append("            ").append(valueType).append(" ").append(valueVar).append("=").append(oldMapVar).append("==null?null:").append(oldMapVar).append(".get(").append(keyVar).append(");\n");
            appendJavaAssignReadExistingValue(sb, valueVar, valueType, bufVar, indent+"            ", hot);
            sb.append(indent).append("            ").append(newMapVar).append(".put(").append(keyVar).append(", ").append(valueVar).append(");\n");
            sb.append(indent).append("        }\n");
            sb.append(indent).append("        if(").append(oldMapVar).append("!=null){\n");
            sb.append(indent).append("            ").append(javaRecycleCollectionStmt(oldMapVar, t)).append(";\n");
            sb.append(indent).append("        }\n");
            sb.append(indent).append("        ").append(targetExpr).append("=").append(newMapVar).append(";\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaWriteValueStatements(StringBuilder sb, String valueExpr, String t, String bufVar, String indent, boolean hot){
            if(isStructType(t) && isInlineStructType(t)){
                appendJavaInlineWriteStructValue(sb, valueExpr, structDef(t), bufVar, indent, hot);
                return;
            }
            if(hot && isHotExpandedType(t)){
                appendJavaHotWriteValue(sb, valueExpr, t, bufVar, indent, true);
                return;
            }
            sb.append(indent).append(writeCursorValue(bufVar, valueExpr, t)).append(";\n");
        }
        static void appendJavaWriteValueStatements(StringBuilder sb, String valueExpr, Field f, String bufVar, String indent, boolean hot){
            if(fieldHasMetadataDrivenCodec(f)){
                sb.append(indent).append(writeCursorValue(bufVar, valueExpr, f)).append(";\n");
                return;
            }
            appendJavaWriteValueStatements(sb, valueExpr, f.type, bufVar, indent, hot);
        }
        static void appendJavaHotReadValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean allowNestedHot){
            // SIMD闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂侀潧妫欑敮鎺楋綖濠靛鏅查柛娑卞墮椤ユ艾鈹戞幊閸婃鎱ㄩ悜钘夌；婵炴垟鎳為崶顒佸仺缂佸瀵ч悗顒勬倵楠炲灝鍔氭い锔诲灣缁牏鈧綆鍋佹禍婊堟煙閺夊灝顣抽柟顔笺偢閺屽秷顧侀柛鎾寸缁绘稒绻濋崶褏鐣哄┑掳鍊曢幊鎰暤娓氣偓閺屾盯鈥﹂幋婵囩亪婵犳鍠栨鎼佲€旈崘顔嘉ч煫鍥ㄦ尵濡诧綁姊洪幖鐐插婵炲鐩幃楣冩偪椤栨ü姹楅梺鍦劋閸ㄥ綊鏁嶅鍫熲拺缂備焦锚婵洦銇勯弴銊ュ籍鐎规洏鍨介弻鍡楊吋閸℃ぞ鐢绘繝鐢靛Т閿曘倝宕幘顔肩煑闁告洦鍨遍悡蹇涙煕閳╁喚娈旈柡鍡悼閳ь剝顫夊ú蹇涘礉鎼淬劌鐒垫い鎺嶈兌閳洟鎳ｉ妶澶嬬厵闁汇値鍨奸崵娆愩亜椤忓嫬鏆ｅ┑鈥崇埣瀹曞崬鈻庤箛锝嗘缂傚倸鍊风粈渚€顢栭崱娑樼闁告挆鍐ㄧ亰婵犵數濮甸懝鍓х矆閸垺鍠愬鑸靛姇绾惧鏌熼崜褏甯涢柛瀣剁節閺屸剝寰勭€ｉ潧鍔屽┑鈽嗗亜閻倸顫忓ú顏勪紶闁靛鍎涢敐鍡欑闁告瑥顦遍惌鎺楁煙瀹曞洤浠遍柡灞芥椤撳ジ宕卞Δ渚囧悑闂傚倷绶氬褔鎮ч崱妞曟椽濡搁埡鍌涙珫濠电姴锕ら悧濠囧煕閹达附鈷戞い鎰╁€曟禒婊堟煠濞茶鐏￠柡鍛埣椤㈡岸鍩€椤掑嫬钃熼柨婵嗩槹閺呮煡鏌涢埄鍐噮闁汇倕瀚伴幃妤冩喆閸曨剛顦梺鍝ュУ閻楃娀濡存担鑲濇棃宕ㄩ鐙呯床婵犳鍠楅敃鈺呭礈濞戙埄鏁婇柛銉墯閳锋帒霉閿濆洨鎽傞柛銈嗙懃铻栭柣妯哄级閹插摜绱掗娆惧殭妞ゆ挸鍚嬪鍕節閸曞墎鍚归梻浣告惈椤︻垶鎮ч崘顔肩柧婵犲﹤鍟伴弳锕傛煛鐏炶鍔滈柣鎾寸懇閺岀喎鐣￠幏灞筋伃闂佺粯甯婄划娆撳蓟瀹ュ牜妾ㄩ梺鍛婃尰閻熲晠鐛繝鍌ゆ建闁逞屽墴婵″瓨鎷呴懖婵囨瀹曘劑顢橀悪鈧Σ瑙勪繆閻愵亜鈧牜鏁幒妤€纾归柟闂磋兌瀹撲線鏌涢鐘插姕闁抽攱甯掗湁闁挎繂鐗婇鐘绘偨椤栨稓鈯曢柕鍥у椤㈡﹢鎮欓弶鎴炵亷婵＄偑鍊戦崹娲€冩繝鍌ゅ殨濠电姵鑹惧敮闂佹寧娲嶉崑鎾寸箾閸繄鐒告慨濠呮缁棃宕卞Δ鈧瀛樼箾閸喐绀嬮柡宀嬬秮楠炴鈧潧鎲￠崚娑㈡⒑鐠団€虫灍闁挎洏鍨介獮鍐ㄢ枎閹惧磭顔岄梺鐟版惈濡瑧鈧灚鐗楃换婵嬫偨闂堟稐娌悷婊勬緲閸熸挳銆佸棰濇晣闁靛繒濮烽崝锕€顪冮妶鍡楃瑐缂佽绻濆畷顖濈疀濞戞瑧鍘遍梺缁橆焾濞呮洜绮堥崼銉︾厱闁圭儤鎸哥粭鎺楁煃鐠囨煡鍙勬鐐叉喘椤㈡棃宕卞▎鎴炴瘞婵犵數濮甸鏍窗濡ゅ啯宕查柛宀€鍋為崕妤呮煕椤愶絾鍎曢柨婵嗘处鐎氭氨鈧懓澹婇崰妤冣偓闈涚焸濮婃椽妫冨☉姘暫闂佸摜鍣ラ崑濠傜暦濠靛棭娼╂い鎾寸矆缁ㄥ姊洪幐搴㈢；婵＄偞甯″畷銉╊敃閿濆嫮绠氶梺鍛婄懃椤︻垱鎱ㄥ鍡╂闁绘劖鎯岄悞浠嬫煃瑜滈崜姘辩矙閹烘鏅俊鐐€曠€涒晠骞愰崜褎顫曢柟鎯х摠婵挳鏌涢幘鏉戠祷闁告捇娼ч埞鎴︽倷瀹割喗效濠电偛寮剁划搴ㄥ礆閹烘绫嶉柛顐亝閺咁亪姊洪柅鐐茶嫰婢ь喗銇勯銏㈢閻撱倖銇勮箛鎾愁仼缂佹劖绋掔换婵嬫偨闂堟刀銏ゆ倵濞戞帗娅婇柟顕€绠栧畷濂稿即閻斿弶瀚奸梻浣告啞缁哄潡宕曢柆宥呭嚑閹兼番鍨荤粻鍓х棯椤撱埄妫戠紒鈾€鍋撴俊銈囧Х閸嬫盯宕幘顔惧祦闁糕剝鍑瑰Σ楣冩⒑閸濆嫭顥滄い鎴濐樀瀵鎮㈤悡搴ｎ槰闁荤姴娉ч崟顒佹瘞濠电姷顣槐鏇㈠磻濡厧鍨濇繛鍡樻尭杩濇繛鎾村焹閸嬫挾鈧鍣崳锝呯暦閻撳簶鏀介柛鈩冪懅瀹曞搫鈹戦敍鍕杭闁稿﹥鐗曢～蹇旂節濮橆剙鍋嶉悷婊冮叄楠炲牓濡搁埡浣侯槰濡炪倖妫佽闁归绮换娑欐綇閸撗呅氬┑鈽嗗亜鐎氭澘鐣烽妷鈺傚仭闁逛絻娅曢弬鈧俊鐐€栧Λ浣规叏閵堝洨绀婇柟杈鹃檮閸嬪倿鏌曢崼婵愭Ч闁绘挾鍠愭穱濠囶敍濞戝崬鍔岄梺鎼炲€栭悷褏妲愰幒妤€鐒垫い鎺戝闁卞洭鏌ｉ弮鍥仩闁伙箑鐗撳濠氬磼濮樺崬顤€婵炴挻纰嶉〃濠傜暦閺囷紕鐤€婵炴垶鐟ч崢鎼佹煟鎼搭垳宀涢柡鍛箘缁綁寮崼鐔哄幐闁诲繒鍋熼崑鎾剁矆鐎ｎ兘鍋撶憴鍕闁告鍥х厴闁硅揪绠戦悙濠勬喐韫囨稒鍋橀柍鍝勫暟绾捐棄霉閿濆懏鎯堝ù婊冨⒔閳ь剝顫夊ú姗€鏁嬮柧鑽ゅ仱閺屾盯寮撮妸銉т哗閺夆晜绻堝娲捶椤撶偛濡洪梺绯曟櫅閻楀棝鈥﹂崶顒€鐓涢柛灞久肩花濠氭⒑閻熺増鎯堢紒澶婄埣瀹曟繂顓兼径瀣幍濡炪倖姊婚弲顐﹀箠閸曨厾纾肩紓浣诡焽閵嗘帡鏌嶈閸撴氨绮欓幒妞烩偓锕傚炊椤掍礁鍓归梺闈涚墕椤︿即鎮￠弴銏＄厪濠电偟鍋撳▍鍐煙閸欏鍔ら柍瑙勫灴閺佸秹宕熼浣圭槗闁诲氦顫夊ú婊堝窗閺嶎厹鈧礁鈽夊鍡樺兊濡炪倖宸婚崑鎾剁磼娓氬洤娅嶆慨濠勭帛閹峰懘鎼归獮搴撳亾婵犲洦鐓曢柟鎯ь嚟缁犵偤鏌曢崱妤€鏆ｇ€规洖宕灒闁惧繐婀遍悰顕€姊绘担鍛婂暈婵炶绠撳畷褰掓焼瀹ュ棗浜楅梺鍝勬川閸嬫劙寮ㄦ禒瀣厽婵☆垵顕х徊缁樸亜韫囷絼閭柡宀嬬節瀹曡精绠涢弮鈧悵鏍磽娴ｄ粙鍝洪悽顖涱殔椤洩绠涘☉妯溾晠鏌曟竟顖氳嫰閸擃剚绻濋悽闈涗粶妞ゆ洦鍘介崚濠勨偓娑櫭肩换鍡涙煕閵夈垺娅呴柛銊︾箞閺岋綁濮€閵忊晜姣岄梺绋款儐閹瑰洭寮幇顓熷劅闁炽儲褰冮崵閬嶆⒒娴ｅ憡鍟為柤褰掔畺閸┾偓妞ゆ垼妫勬禍绶€[]闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤濠€閬嶅焵椤掑倹鍤€閻庢凹鍙冨畷宕囧鐎ｃ劋姹楅梺鍦劋閸ㄥ綊宕愰悙鐑樺仭婵犲﹤瀚惌濠囨婢舵劖鐓涚€广儱楠搁獮妤呮煟閹惧磭绠伴柍瑙勫灴閹瑩妫冨☉妤€顥氭繝鐢靛仜閹冲骸螞濠靛钃熸繛鎴炵懅缁♀偓闂佺鏈粙鎴炵閹绢喗鈷戦柛婵嗗椤箓鏌涢弮鈧崹鐢割敋閿濆鏁嬮柍褜鍓欓悾鐑藉箳閹存梹顫嶅┑鐐叉閸ㄩ潧鈽夎濮婂宕掑▎鎺戝帯缂備緡鍣崹鎶藉箲閵忋倕纾奸柣鎰綑娴滎垱绻濋棃娑樷偓濠氣€﹂崼銉﹀珔闁绘柨鎽滅粻楣冩煙鐎涙鎳冮柣蹇婃櫊閺岋綁骞掗幘娣虎闂佸搫鏈惄顖炵嵁閸ヮ剙绀傞柛婵勫劚閸ゎ剟姊绘笟鈧濠氬箑閵夆晛鐐婇柕濞垮灪鐎氬ジ姊绘担渚敯闁稿鍔欏畷鎴濃槈濞嗗海绠氶梺浼欑到閻偐澹曟總鍛婂仯闁搞儯鍔岀徊缁樸亜韫囷絽寮柡灞剧洴閸╃偤骞嗚婢规洖鈹戦敍鍕杭闁稿﹥鐗曢蹇旂節濮橆剛锛涢梺瑙勫劤婢у海澹曟總鍛婄厽婵☆垰鐏濋惃娲极閸儲鍊甸悷娆忓缁€鍐╃箾閼碱剙鏋涚€殿喖顭峰畷鍗炍旀繝鍌涘€梻浣虹《閸撴繈濡甸悙鐢电＞闁哄洢鍨洪埛鎺戔攽閻樻煡顎楀ù婊勭矋缁绘稓鎷犺椤╊剛绱掗鐣屾噰妤犵偞甯掕灃闁逞屽墰婢规洜绱掑Ο闀愮盎闂婎偄娲﹂幐鐐櫠濞戙垺鐓熼柟鎯у暱閺嗙喖鏌熼懠顒夌劸妞ゎ厹鍔戝畷鐓庘攽閸偅肖濠电姷鏁搁崑娑樜涘▎鎾崇闁圭虎鍠栭悞鍨亜閹哄秶鍔嶆い銉ョ墢閳ь剝顫夊ú姗€鏁冮姀銈冣偓浣糕枎閹惧啿绨ユ繝銏ｎ嚃閸ㄦ澘煤閿曞倹鍋傞柡鍥ュ灪閻撳啴鏌嶆潪鎵槮闁哄鍊栫换娑㈠醇閻旂硶鎷婚梺璇″枤婢ф缂撴禒瀣窛濠电偟鍋撶€氳棄鈹戦悙鑸靛涧缂佹煡绠栭獮鍐醇閺囩偟锛涢梺闈涢獜缁辨洜绮婚幆顬″綊鏁愰崨顓熸瘣闂佺娅曢崝妤冨垝閸喓绡€闁稿本顨嗛弬鈧梻浣虹帛钃辩憸鏉垮暣閸┾偓妞ゆ巻鍋撴い顓炲槻椤曪綁骞庨懞銉︽珖闂佺鏈粙鎾诲储閻㈠憡鐓涘璺猴功婢ф洖顭胯缁瑩骞冮悙顒佺秶闁冲搫鍟伴敍婊堟⒑缂佹◤顏堝箹椤愶箑瑙﹂柛锔诲幘绾惧ジ寮堕崼娑樺閻忓繋鍗抽弻鐔风暋閻楀牆娅ら梺閫涚┒閸旀垿鐛€ｎ噮鏁囨繝闈涚墕閸ゎ剛绱撻崒娆戭槮闁稿﹤鎽滅划鏃堝箻椤斿厜鍋撻敃鍌毼╅柍杞扮缁愭稑顪冮妶鍡欏缂侇噯绲垮Σ鎰板礃濞村鏂€闂佺粯鍔橀崺鏍亹瑜忕槐鎺楁嚑鐠虹洅鎾剁磼閺冨倸鏋戠紒缁樼箞瀹曟儼顦撮柨娑氬枛濮婇缚銇愰幒鎴滃枈闂佸憡顭堥～澶岀矉瀹ュ拋鐓ラ柛顐ゅ枔閸樼敻鏌ｆ惔锝嗘毄妞ゎ厼鐗婄粋鎺曨樄闁哄瞼鍠栧畷姗€骞撻幒婵呯磾闂備浇顕栭崰妤呫€冩繝鍋芥盯宕橀妸銏☆潔闂佸搫璇為崒姘亰婵犲痉鏉库偓妤佹叏閻戣棄纾婚柣鎰€€閳ь剨绠撳畷鍫曞煛閸屻倖缍楅梻浣告贡閸庛倝宕甸敃鍌涘亜闁稿繒鍘ф禍妤呮煙閼圭増褰х紒杈ㄦ礋椤?
            if(SIMD_ENABLED && isPrimitiveByteArray(t)){
                appendJavaSIMDReadByteArray(sb, targetExpr, t, bufVar, indent);
                return;
            }
            if(SIMD_ENABLED && isHotSimdRawPrimitiveArrayType(t)){
                appendJavaFixedSimdReadArrayField(sb, targetExpr, t, bufVar, indent);
                return;
            }
            if(isHotObjectArrayType(t)){
                String inner=t.substring(0, t.length()-2).trim();
                String arrayType=mapType(t);
                String countVar=childVar(targetExpr, "count");
                String tmpVar=childVar(targetExpr, "tmp");
                String indexVar=childVar(targetExpr, "index");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
                sb.append(indent).append("    ").append(arrayType).append(" ").append(tmpVar).append("=").append(javaArrayAllocationExpr(t, countVar)).append(";\n");
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendJavaAssignReadValue(sb, tmpVar+"["+indexVar+"]", inner, bufVar, indent+"        ", allowNestedHot);
                sb.append(indent).append("    }\n");
                sb.append(indent).append("    ").append(targetExpr).append("=").append(tmpVar).append(";\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isListLikeType(t)){
                String inner=genericBody(t).trim();
                String listType=mapType(t);
                String countVar=childVar(targetExpr, "count");
                String tmpVar=childVar(targetExpr, "tmp");
                String indexVar=childVar(targetExpr, "index");
                String elemVar=childVar(targetExpr, "elem");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
                if("LinkedList".equals(canonicalContainerType(t))){
                    sb.append(indent).append("    ").append(listType).append(" ").append(tmpVar).append("=new LinkedList<>();\n");
                }else{
                    sb.append(indent).append("    ").append(listType).append(" ").append(tmpVar).append("=").append(javaBorrowCollectionExpr(t, countVar)).append(";\n");
                }
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendJavaReadValueToLocal(sb, elemVar, inner, bufVar, indent+"        ", allowNestedHot);
                sb.append(indent).append("        ").append(tmpVar).append(".add(").append(elemVar).append(");\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("    ").append(targetExpr).append("=").append(tmpVar).append(";\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isSetLikeType(t)){
                String inner=genericBody(t).trim();
                String setType=mapType(t);
                String countVar=childVar(targetExpr, "count");
                String tmpVar=childVar(targetExpr, "tmp");
                String indexVar=childVar(targetExpr, "index");
                String elemVar=childVar(targetExpr, "elem");
                String capacity=javaHashCapacityExpr(countVar);
                sb.append(indent).append("{\n");
                sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
                if("LinkedHashSet".equals(canonicalContainerType(t))){
                    sb.append(indent).append("    ").append(setType).append(" ").append(tmpVar).append("=").append(javaBorrowCollectionExpr(t, countVar)).append(";\n");
                }else{
                    sb.append(indent).append("    ").append(setType).append(" ").append(tmpVar).append("=").append(javaBorrowCollectionExpr(t, countVar)).append(";\n");
                }
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendJavaReadValueToLocal(sb, elemVar, inner, bufVar, indent+"        ", allowNestedHot);
                sb.append(indent).append("        ").append(tmpVar).append(".add(").append(elemVar).append(");\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("    ").append(targetExpr).append("=").append(tmpVar).append(";\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isQueueLikeType(t)){
                String inner=genericBody(t).trim();
                String queueType=mapType(t);
                String countVar=childVar(targetExpr, "count");
                String tmpVar=childVar(targetExpr, "tmp");
                String indexVar=childVar(targetExpr, "index");
                String elemVar=childVar(targetExpr, "elem");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
                sb.append(indent).append("    ").append(queueType).append(" ").append(tmpVar).append("=").append(javaBorrowCollectionExpr(t, countVar)).append(";\n");
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendJavaReadValueToLocal(sb, elemVar, inner, bufVar, indent+"        ", allowNestedHot);
                sb.append(indent).append("        ").append(tmpVar).append(".add(").append(elemVar).append(");\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("    ").append(targetExpr).append("=").append(tmpVar).append(";\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isMapLikeType(t)){
                List<String> kv=splitTopLevel(genericBody(t), ',');
                String keyType=kv.get(0).trim();
                String valueType=kv.get(1).trim();
                String mapType=mapType(t);
                String countVar=childVar(targetExpr, "count");
                String tmpVar=childVar(targetExpr, "tmp");
                String indexVar=childVar(targetExpr, "index");
                String keyVar=childVar(targetExpr, "key");
                String valueVar=childVar(targetExpr, "value");
                String capacity=javaHashCapacityExpr(countVar);
                sb.append(indent).append("{\n");
                sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
                if("LinkedHashMap".equals(canonicalContainerType(t))){
                    sb.append(indent).append("    ").append(mapType).append(" ").append(tmpVar).append("=").append(javaBorrowCollectionExpr(t, countVar)).append(";\n");
                }else{
                    sb.append(indent).append("    ").append(mapType).append(" ").append(tmpVar).append("=").append(javaBorrowCollectionExpr(t, countVar)).append(";\n");
                }
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendJavaReadValueToLocal(sb, keyVar, keyType, bufVar, indent+"        ", allowNestedHot);
                appendJavaReadValueToLocal(sb, valueVar, valueType, bufVar, indent+"        ", allowNestedHot);
                sb.append(indent).append("        ").append(tmpVar).append(".put(").append(keyVar).append(", ").append(valueVar).append(");\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("    ").append(targetExpr).append("=").append(tmpVar).append(";\n");
                sb.append(indent).append("}\n");
                return;
            }
            sb.append(indent).append(targetExpr).append("=").append(readCursorValue(bufVar, t)).append(";\n");
        }
        static void appendJavaHotWriteValue(StringBuilder sb, String valueExpr, String t, String bufVar, String indent, boolean allowNestedHot){
            if(SIMD_ENABLED && isPrimitiveByteArray(t)){
                sb.append(indent).append("ByteIO.writeBytes(").append(bufVar).append(",").append(valueExpr).append(");\n");
                return;
            }
            if(SIMD_ENABLED && isHotSimdRawPrimitiveArrayType(t)){
                appendJavaFixedSimdWriteArrayField(sb, valueExpr, t, bufVar, indent);
                return;
            }
            if(isHotObjectArrayType(t)){
                String inner=t.substring(0, t.length()-2).trim();
                String arrayType=mapType(t);
                String arrayVar=childVar(valueExpr, "array");
                String countVar=childVar(valueExpr, "count");
                String indexVar=childVar(valueExpr, "index");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    ").append(arrayType).append(" ").append(arrayVar).append("=").append(valueExpr).append(";\n");
                sb.append(indent).append("    int ").append(countVar).append("=").append(arrayVar).append("==null?0:").append(arrayVar).append(".length;\n");
                sb.append(indent).append("    ByteIO.writeSize(").append(bufVar).append(", ").append(countVar).append(");\n");
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendJavaWriteValueStatements(sb, arrayVar+"["+indexVar+"]", inner, bufVar, indent+"        ", allowNestedHot);
                sb.append(indent).append("    }\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isListLikeType(t)){
                String inner=genericBody(t).trim();
                String listType=mapType(t);
                String containerType=canonicalContainerType(t);
                String listVar=childVar(valueExpr, "list");
                String countVar=childVar(valueExpr, "count");
                String indexVar=childVar(valueExpr, "index");
                String elemVar=childVar(valueExpr, "elem");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    ").append(listType).append(" ").append(listVar).append("=").append(valueExpr).append(";\n");
                sb.append(indent).append("    int ").append(countVar).append("=").append(listVar).append("==null?0:").append(listVar).append(".size();\n");
                sb.append(indent).append("    ByteIO.writeSize(").append(bufVar).append(", ").append(countVar).append(");\n");
                sb.append(indent).append("    if(").append(countVar).append("!=0){\n");
                if(isSpecializedIntListType(t)){
                    sb.append(indent).append("        if(").append(listVar).append(" instanceof IntArrayList){\n");
                    sb.append(indent).append("            IntArrayList __list=(IntArrayList)").append(listVar).append(";\n");
                    sb.append(indent).append("            for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                    sb.append(indent).append("                ByteIO.writeInt(").append(bufVar).append(", __list.getInt(").append(indexVar).append("));\n");
                    sb.append(indent).append("            }\n");
                    sb.append(indent).append("        }else ");
                }else if(isSpecializedLongListType(t)){
                    sb.append(indent).append("        if(").append(listVar).append(" instanceof LongArrayList){\n");
                    sb.append(indent).append("            LongArrayList __list=(LongArrayList)").append(listVar).append(";\n");
                    sb.append(indent).append("            for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                    sb.append(indent).append("                ByteIO.writeLong(").append(bufVar).append(", __list.getLong(").append(indexVar).append("));\n");
                    sb.append(indent).append("            }\n");
                    sb.append(indent).append("        }else ");
                }
                if("LinkedList".equals(containerType) || "Collection".equals(containerType)){
                    sb.append(indent).append("        for(").append(mapType(inner)).append(" ").append(elemVar).append(": ").append(listVar).append("){\n");
                    appendJavaWriteValueStatements(sb, elemVar, inner, bufVar, indent+"            ", allowNestedHot);
                    sb.append(indent).append("        }\n");
                }else{
                    sb.append(indent).append("        if(").append(listVar).append(" instanceof RandomAccess){\n");
                    sb.append(indent).append("            for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                    appendJavaWriteValueStatements(sb, listVar+".get("+indexVar+")", inner, bufVar, indent+"                ", allowNestedHot);
                    sb.append(indent).append("            }\n");
                    sb.append(indent).append("        }else{\n");
                    sb.append(indent).append("            for(").append(mapType(inner)).append(" ").append(elemVar).append(": ").append(listVar).append("){\n");
                    appendJavaWriteValueStatements(sb, elemVar, inner, bufVar, indent+"                ", allowNestedHot);
                    sb.append(indent).append("            }\n");
                    sb.append(indent).append("        }\n");
                }
                sb.append(indent).append("    }\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isSetLikeType(t)){
                String inner=genericBody(t).trim();
                String setType=mapType(t);
                String setVar=childVar(valueExpr, "set");
                String countVar=childVar(valueExpr, "count");
                String elemVar=childVar(valueExpr, "elem");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    ").append(setType).append(" ").append(setVar).append("=").append(valueExpr).append(";\n");
                sb.append(indent).append("    int ").append(countVar).append("=").append(setVar).append("==null?0:").append(setVar).append(".size();\n");
                sb.append(indent).append("    ByteIO.writeSize(").append(bufVar).append(", ").append(countVar).append(");\n");
                sb.append(indent).append("    if(").append(countVar).append("!=0){\n");
                sb.append(indent).append("        for(").append(mapType(inner)).append(" ").append(elemVar).append(": ").append(setVar).append("){\n");
                appendJavaWriteValueStatements(sb, elemVar, inner, bufVar, indent+"            ", allowNestedHot);
                sb.append(indent).append("        }\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isQueueLikeType(t)){
                String inner=genericBody(t).trim();
                String queueType=mapType(t);
                String queueVar=childVar(valueExpr, "queue");
                String countVar=childVar(valueExpr, "count");
                String elemVar=childVar(valueExpr, "elem");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    ").append(queueType).append(" ").append(queueVar).append("=").append(valueExpr).append(";\n");
                sb.append(indent).append("    int ").append(countVar).append("=").append(queueVar).append("==null?0:").append(queueVar).append(".size();\n");
                sb.append(indent).append("    ByteIO.writeSize(").append(bufVar).append(", ").append(countVar).append(");\n");
                sb.append(indent).append("    if(").append(countVar).append("!=0){\n");
                sb.append(indent).append("        for(").append(mapType(inner)).append(" ").append(elemVar).append(": ").append(queueVar).append("){\n");
                appendJavaWriteValueStatements(sb, elemVar, inner, bufVar, indent+"            ", allowNestedHot);
                sb.append(indent).append("        }\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isMapLikeType(t)){
                List<String> kv=splitTopLevel(genericBody(t), ',');
                String keyType=kv.get(0).trim();
                String valueType=kv.get(1).trim();
                String mapType=mapType(t);
                String mapVar=childVar(valueExpr, "map");
                String countVar=childVar(valueExpr, "count");
                String entryVar=childVar(valueExpr, "entry");
                String cursorVar=childVar(valueExpr, "cursor");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    ").append(mapType).append(" ").append(mapVar).append("=").append(valueExpr).append(";\n");
                sb.append(indent).append("    int ").append(countVar).append("=").append(mapVar).append("==null?0:").append(mapVar).append(".size();\n");
                sb.append(indent).append("    ByteIO.writeSize(").append(bufVar).append(", ").append(countVar).append(");\n");
                sb.append(indent).append("    if(").append(countVar).append("!=0){\n");
                if(isSpecializedIntObjectMapType(t)){
                    sb.append(indent).append("        if(").append(mapVar).append(" instanceof IntObjectHashMap){\n");
                    sb.append(indent).append("            IntObjectHashMap.Cursor<").append(mapType(valueType)).append("> ").append(cursorVar).append("=((IntObjectHashMap<").append(mapType(valueType)).append(">)").append(mapVar).append(").cursor();\n");
                    sb.append(indent).append("            while(").append(cursorVar).append(".advance()){\n");
                    sb.append(indent).append("                ByteIO.writeInt(").append(bufVar).append(", ").append(cursorVar).append(".key());\n");
                    appendJavaWriteValueStatements(sb, cursorVar+".value()", valueType, bufVar, indent+"                ", allowNestedHot);
                    sb.append(indent).append("            }\n");
                    sb.append(indent).append("        }else{\n");
                    sb.append(indent).append("            for(Map.Entry<").append(wrapGeneric(keyType)).append(",").append(wrapGeneric(valueType)).append("> ").append(entryVar).append(": ").append(mapVar).append(".entrySet()){\n");
                    appendJavaWriteValueStatements(sb, entryVar+".getKey()", keyType, bufVar, indent+"                ", allowNestedHot);
                    appendJavaWriteValueStatements(sb, entryVar+".getValue()", valueType, bufVar, indent+"                ", allowNestedHot);
                    sb.append(indent).append("            }\n");
                    sb.append(indent).append("        }\n");
                }else{
                    sb.append(indent).append("        for(Map.Entry<").append(wrapGeneric(keyType)).append(",").append(wrapGeneric(valueType)).append("> ").append(entryVar).append(": ").append(mapVar).append(".entrySet()){\n");
                    appendJavaWriteValueStatements(sb, entryVar+".getKey()", keyType, bufVar, indent+"            ", allowNestedHot);
                    appendJavaWriteValueStatements(sb, entryVar+".getValue()", valueType, bufVar, indent+"            ", allowNestedHot);
                    sb.append(indent).append("        }\n");
                }
                sb.append(indent).append("    }\n");
                sb.append(indent).append("}\n");
                return;
            }
            sb.append(indent).append(writeCursorValue(bufVar, valueExpr, t)).append(";\n");
        }
        static String javaHashCapacityExpr(String sizeExpr){
            return "(int)Math.max(4L, (((long)"+sizeExpr+") * 4L + 2L) / 3L)";
        }
        static String javaBorrowCollectionExpr(String t, String sizeExpr){
            String inner=mapType(genericBody(t).trim());
            if(isListLikeType(t)){
                if("LinkedList".equals(canonicalContainerType(t))){
                    return "new LinkedList<"+inner+">()";
                }
                if(isSpecializedIntListType(t)){
                    return "ByteIO.borrowIntArrayList("+sizeExpr+")";
                }
                if(isSpecializedLongListType(t)){
                    return "ByteIO.borrowLongArrayList("+sizeExpr+")";
                }
                return "ByteIO.borrowArrayList("+sizeExpr+")";
            }
            if(isSetLikeType(t)){
                if("LinkedHashSet".equals(canonicalContainerType(t))){
                    return "ByteIO.borrowLinkedHashSet("+sizeExpr+")";
                }
                return "ByteIO.borrowHashSet("+sizeExpr+")";
            }
            if(isQueueLikeType(t)){
                return "ByteIO.borrowArrayDeque("+sizeExpr+")";
            }
            if(isMapLikeType(t)){
                List<String> kv=splitTopLevel(genericBody(t), ',');
                String kt=mapType(kv.get(0).trim());
                String vt=mapType(kv.get(1).trim());
                if("LinkedHashMap".equals(canonicalContainerType(t))){
                    return "ByteIO.borrowLinkedHashMap("+sizeExpr+")";
                }
                if(isSpecializedIntIntMapType(t)){
                    return "ByteIO.borrowIntIntHashMap("+sizeExpr+")";
                }
                if(isSpecializedIntLongMapType(t)){
                    return "ByteIO.borrowIntLongHashMap("+sizeExpr+")";
                }
                if(isSpecializedIntObjectMapType(t)){
                    return "ByteIO.borrowIntObjectHashMap("+sizeExpr+")";
                }
                return "ByteIO.borrowHashMap("+sizeExpr+")";
            }
            return javaDefaultValueExpr(t);
        }
        static String javaRecycleCollectionStmt(String valueExpr, String t){
            if(isListLikeType(t)){
                if("LinkedList".equals(canonicalContainerType(t))){
                    return valueExpr+".clear()";
                }
                if(isSpecializedIntListType(t)){
                    return "ByteIO.recycleIntArrayList("+valueExpr+")";
                }
                if(isSpecializedLongListType(t)){
                    return "ByteIO.recycleLongArrayList("+valueExpr+")";
                }
                return "ByteIO.recycleArrayList("+valueExpr+")";
            }
            if(isSetLikeType(t)){
                if("LinkedHashSet".equals(canonicalContainerType(t))){
                    return "ByteIO.recycleLinkedHashSet("+valueExpr+")";
                }
                return "ByteIO.recycleHashSet("+valueExpr+")";
            }
            if(isQueueLikeType(t)){
                return "ByteIO.recycleArrayDeque("+valueExpr+")";
            }
            if(isMapLikeType(t)){
                if("LinkedHashMap".equals(canonicalContainerType(t))){
                    return "ByteIO.recycleLinkedHashMap("+valueExpr+")";
                }
                if(isSpecializedIntIntMapType(t)){
                    return "ByteIO.recycleIntIntHashMap("+valueExpr+")";
                }
                if(isSpecializedIntLongMapType(t)){
                    return "ByteIO.recycleIntLongHashMap("+valueExpr+")";
                }
                if(isSpecializedIntObjectMapType(t)){
                    return "ByteIO.recycleIntObjectHashMap("+valueExpr+")";
                }
                return "ByteIO.recycleHashMap("+valueExpr+")";
            }
            return valueExpr+".clear()";
        }
        static void appendJavaInlineReadStructValue(StringBuilder sb, String targetExpr, Struct nested, String bufVar, String indent, boolean hot){
            if(nested==null){
                throw new IllegalStateException("missing inline struct metadata for "+targetExpr);
            }
            if(isFixedStruct(nested)){
                String reuseVar=childVar(targetExpr, "reuse");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    ").append(nested.name).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                sb.append(indent).append("    if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("        ").append(reuseVar).append("=new ").append(nested.name).append("();\n");
                sb.append(indent).append("        ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("    }\n");
                appendJavaFixedReadStructInto(sb, nested, reuseVar, bufVar, indent+"    ", hot);
                sb.append(indent).append("}\n");
                return;
            }
            List<Field> nestedPresence=presenceFields(nested.fields);
            String reuseVar=childVar(targetExpr, "reuse");
            String presenceVar=childVar(targetExpr, "presence");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    ").append(nested.name).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("    if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("        ").append(reuseVar).append("=new ").append(nested.name).append("();\n");
            sb.append(indent).append("        ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("    }\n");
            appendJavaPresenceReadPrelude(sb, nestedPresence.size(), bufVar, indent+"    ", "ByteIO", presenceVar);
            int presenceIndex=0;
            for(Field field: nested.fields){
                String fieldExpr=reuseVar+"."+field.name;
                if(isPresenceTrackedType(field.type)){
                    String presentExpr=javaPresenceExpr(presenceVar, presenceIndex++, nestedPresence.size());
                    if(isOptionalType(field.type)){
                        String inner=genericBody(field.type).trim();
                        sb.append(indent).append("    if(").append(presentExpr).append("){\n");
                        if(isJavaReusableReadTargetType(inner)){
                            String valueVar=childVar(fieldExpr, "value");
                            sb.append(indent).append("        ").append(mapType(inner)).append(" ").append(valueVar).append("=")
                                    .append(optionalPresentExpr(fieldExpr)).append(" ? ").append(fieldExpr).append(".get() : null;\n");
                            appendJavaAssignReadExistingValue(sb, valueVar, inner, bufVar, indent+"        ", hot);
                            sb.append(indent).append("        ").append(fieldExpr).append("=Optional.ofNullable(").append(valueVar).append(");\n");
                        }else{
                            appendJavaReadValueToLocal(sb, "__value", inner, bufVar, indent+"        ", hot);
                            sb.append(indent).append("        ").append(fieldExpr).append("=Optional.ofNullable(__value);\n");
                        }
                        sb.append(indent).append("    }else{\n");
                        sb.append(indent).append("        ").append(fieldExpr).append("=Optional.empty();\n");
                        sb.append(indent).append("    }\n");
                    }else{
                        sb.append(indent).append("    if(").append(presentExpr).append("){\n");
                        appendJavaAssignReadExistingValue(sb, fieldExpr, field, bufVar, indent+"        ", hot);
                        sb.append(indent).append("    }else{\n");
                        appendJavaResetReadValue(sb, fieldExpr, field, indent+"        ");
                        sb.append(indent).append("    }\n");
                    }
                }else{
                    appendJavaAssignReadExistingValue(sb, fieldExpr, field, bufVar, indent+"    ", hot);
                }
            }
            sb.append(indent).append("}\n");
        }
        static void appendJavaInlineWriteStructValue(StringBuilder sb, String valueExpr, Struct nested, String bufVar, String indent, boolean hot){
            if(nested==null){
                throw new IllegalStateException("missing inline struct metadata for "+valueExpr);
            }
            String valueVar=childVar(valueExpr, "value");
            String presenceVar=childVar(valueExpr, "presence");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    ").append(nested.name).append(" ").append(valueVar).append("=").append(valueExpr).append(";\n");
            sb.append(indent).append("    if(").append(valueVar).append("==null){\n");
                sb.append(indent).append("        ").append(valueVar).append("=new ").append(nested.name).append("();\n");
            sb.append(indent).append("    }\n");
            if(isFixedStruct(nested)){
                appendJavaFixedWriteStructValue(sb, nested, valueVar, bufVar, indent+"    ", hot);
            }else{
                List<Field> nestedPresence=presenceFields(nested.fields);
                appendJavaPresenceWritePrelude(sb, nestedPresence, valueVar+".", bufVar, indent+"    ", "ByteIO", presenceVar);
                for(Field field: nested.fields){
                    String fieldExpr=valueVar+"."+field.name;
                    if(isOptionalType(field.type)){
                        sb.append(indent).append("    if(").append(optionalPresentExpr(fieldExpr)).append("){\n");
                        appendJavaWriteValueStatements(sb, fieldExpr+".get()", genericBody(field.type).trim(), bufVar, indent+"        ", hot);
                        sb.append(indent).append("    }\n");
                    }else if(isPresenceTrackedType(field.type)){
                        sb.append(indent).append("    if(").append(javaHasWireValueExpr(fieldExpr, field)).append("){\n");
                        appendJavaWriteValueStatements(sb, fieldExpr, field, bufVar, indent+"        ", hot);
                        sb.append(indent).append("    }\n");
                    }else{
                        appendJavaWriteValueStatements(sb, fieldExpr, field, bufVar, indent+"    ", hot);
                    }
                }
            }
            sb.append(indent).append("}\n");
        }
        static void appendJavaFixedReadStructInto(StringBuilder sb, Struct s, String targetExpr, String bufVar, String indent, boolean hot){
            for(Field field: s.fields){
                appendJavaFixedReadField(sb, targetExpr+"."+field.name, field, bufVar, indent, hot);
            }
        }
        static void appendJavaFixedWriteStructValue(StringBuilder sb, Struct s, String valueExpr, String bufVar, String indent, boolean hot){
            for(Field field: s.fields){
                appendJavaFixedWriteField(sb, valueExpr+"."+field.name, field, bufVar, indent, hot);
            }
        }
        static void appendJavaFixedReadField(StringBuilder sb, String targetExpr, Field f, String bufVar, String indent, boolean hot){
            if(fieldHasMetadataDrivenCodec(f)){
                sb.append(indent).append(targetExpr).append("=").append(readCursorValue(bufVar, f)).append(";\n");
                return;
            }
            appendJavaFixedReadField(sb, targetExpr, f.type, bufVar, indent, hot);
        }
        static void appendJavaFixedReadField(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean hot){
            if(SIMD_ENABLED && isFixedSimdPrimitiveArray(t)){
                appendJavaFixedSimdReadArrayField(sb, targetExpr, t, bufVar, indent);
                return;
            }
            if(t.endsWith("[]")){
                sb.append(indent).append(targetExpr).append("=").append(readFixedArrayValue(bufVar, t)).append(";\n");
                return;
            }
            if(isStructType(t)){
                Struct nested=structDef(t);
                String reuseVar=childVar(targetExpr, "reuse");
                sb.append(indent).append(t).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                sb.append(indent).append("if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("    ").append(reuseVar).append("=new ").append(t).append("();\n");
                sb.append(indent).append("    ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("}\n");
                if(isInlineStructType(t)){
                    appendJavaFixedReadStructInto(sb, nested, reuseVar, bufVar, indent, hot);
                }else{
                    sb.append(indent).append(t).append(".readInto(").append(bufVar).append(", ").append(reuseVar).append(");\n");
                }
                return;
            }
            sb.append(indent).append(targetExpr).append("=").append(readFixedCursorValue(bufVar, t)).append(";\n");
        }
        static void appendJavaFixedWriteField(StringBuilder sb, String valueExpr, Field f, String bufVar, String indent, boolean hot){
            if(fieldHasMetadataDrivenCodec(f)){
                sb.append(indent).append(writeCursorValue(bufVar, valueExpr, f)).append(";\n");
                return;
            }
            appendJavaFixedWriteField(sb, valueExpr, f.type, bufVar, indent, hot);
        }
        static void appendJavaFixedWriteField(StringBuilder sb, String valueExpr, String t, String bufVar, String indent, boolean hot){
            if(SIMD_ENABLED && isFixedSimdPrimitiveArray(t)){
                appendJavaFixedSimdWriteArrayField(sb, valueExpr, t, bufVar, indent);
                return;
            }
            if(t.endsWith("[]")){
                sb.append(indent).append(writeFixedArrayValue(bufVar, valueExpr, t)).append(";\n");
                return;
            }
            if(isStructType(t)){
                Struct nested=structDef(t);
                String valueVar=childVar(valueExpr, "value");
                sb.append(indent).append(t).append(" ").append(valueVar).append("=").append(valueExpr).append(";\n");
                sb.append(indent).append("if(").append(valueVar).append("==null){ ").append(valueVar).append("=new ").append(t).append("(); }\n");
                if(isInlineStructType(t)){
                    appendJavaFixedWriteStructValue(sb, nested, valueVar, bufVar, indent, hot);
                }else{
                    sb.append(indent).append(valueVar).append(".writeTo(").append(bufVar).append(");\n");
                }
                return;
            }
            sb.append(indent).append(writeFixedCursorValue(bufVar, valueExpr, t)).append(";\n");
        }
        static void appendJavaFixedSimdReadArrayField(StringBuilder sb, String targetExpr, String t, String bufVar, String indent){
            String inner=t.substring(0, t.length()-2).trim();
            String countVar=childVar(targetExpr, "count");
            String tmpVar=childVar(targetExpr, "tmp");
            String offsetVar=childVar(targetExpr, "offset");
            String segmentVar=childVar(targetExpr, "segment");
            String boundVar=childVar(targetExpr, "bound");
            String byteOffsetVar=childVar(targetExpr, "byteOffset");
            String indexVar=childVar(targetExpr, "index");
            String bytesConst;
            String speciesVar;
            String vectorClass;
            String fallbackRead;
            String scalarGetter;
            if(inner.equals("int")){
                bytesConst="Integer.BYTES";
                speciesVar="FIXED_INT_SPECIES";
                vectorClass="IntVector";
                fallbackRead="ByteIO.readFixedIntArray("+bufVar+")";
                scalarGetter="getInt()";
            }else if(inner.equals("long")){
                bytesConst="Long.BYTES";
                speciesVar="FIXED_LONG_SPECIES";
                vectorClass="LongVector";
                fallbackRead="ByteIO.readFixedLongArray("+bufVar+")";
                scalarGetter="getLong()";
            }else if(inner.equals("float")){
                bytesConst="Float.BYTES";
                speciesVar="FIXED_FLOAT_SPECIES";
                vectorClass="FloatVector";
                fallbackRead="ByteIO.readFixedFloatArray("+bufVar+")";
                scalarGetter="getFloat()";
            }else{
                bytesConst="Double.BYTES";
                speciesVar="FIXED_DOUBLE_SPECIES";
                vectorClass="DoubleVector";
                fallbackRead="ByteIO.readFixedDoubleArray("+bufVar+")";
                scalarGetter="getDouble()";
            }
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    ").append(mapType(inner)).append("[] ").append(tmpVar).append("=new ").append(mapType(inner)).append("[").append(countVar).append("];\n");
            sb.append(indent).append("    if(").append(countVar).append("!=0){\n");
            sb.append(indent).append("        if((").append(bufVar).append(" instanceof ArrayByteCursor) || (").append(bufVar).append(" instanceof LinearByteBuffer) || (").append(bufVar).append(" instanceof NettyCursor && ((NettyCursor)").append(bufVar).append(").unwrap().hasArray())){\n");
            sb.append(indent).append("            int ").append(offsetVar).append("=(").append(bufVar).append(" instanceof ArrayByteCursor) ? ((ArrayByteCursor)").append(bufVar).append(").getOffset() : (").append(bufVar).append(" instanceof NettyCursor ? ((NettyCursor)").append(bufVar).append(").unwrap().arrayOffset()+((NettyCursor)").append(bufVar).append(").unwrap().readerIndex() : ((LinearByteBuffer)").append(bufVar).append(").readerIndex());\n");
            sb.append(indent).append("            MemorySegment ").append(segmentVar).append("=MemorySegment.ofArray(").append(bufVar).append(".array());\n");
            sb.append(indent).append("            int ").append(boundVar).append("=").append(speciesVar).append(".loopBound(").append(countVar).append(");\n");
            sb.append(indent).append("            long ").append(byteOffsetVar).append("=").append(offsetVar).append(";\n");
            sb.append(indent).append("            for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(boundVar).append(";").append(indexVar).append("+=").append(speciesVar).append(".length()){\n");
            sb.append(indent).append("                ").append(vectorClass).append(".fromMemorySegment(").append(speciesVar).append(", ").append(segmentVar).append(", ").append(byteOffsetVar).append(", ByteOrder.BIG_ENDIAN).intoArray(").append(tmpVar).append(", ").append(indexVar).append(");\n");
            sb.append(indent).append("                ").append(byteOffsetVar).append(" += (long)").append(speciesVar).append(".length() * ").append(bytesConst).append(";\n");
            sb.append(indent).append("            }\n");
            sb.append(indent).append("            for(int ").append(indexVar).append("=").append(boundVar).append(";").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            sb.append(indent).append("                ").append(tmpVar).append("[").append(indexVar).append("]=java.nio.ByteBuffer.wrap(").append(bufVar).append(".array(), ").append(offsetVar).append("+(").append(indexVar).append("*").append(bytesConst).append("), ").append(bytesConst).append(").order(ByteOrder.BIG_ENDIAN).").append(scalarGetter).append(";\n");
            sb.append(indent).append("            }\n");
            sb.append(indent).append("            ").append(bufVar).append(".skip(").append(countVar).append("*").append(bytesConst).append(");\n");
            sb.append(indent).append("        }else{\n");
            sb.append(indent).append("            ").append(tmpVar).append("=").append(fallbackRead).append(";\n");
            sb.append(indent).append("        }\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("    ").append(targetExpr).append("=").append(tmpVar).append(";\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaFixedSimdWriteArrayField(StringBuilder sb, String valueExpr, String t, String bufVar, String indent){
            String inner=t.substring(0, t.length()-2).trim();
            String valuesVar=childVar(valueExpr, "values");
            String countVar=childVar(valueExpr, "count");
            String offsetVar=childVar(valueExpr, "offset");
            String segmentVar=childVar(valueExpr, "segment");
            String boundVar=childVar(valueExpr, "bound");
            String byteOffsetVar=childVar(valueExpr, "byteOffset");
            String indexVar=childVar(valueExpr, "index");
            String bytesConst;
            String speciesVar;
            String vectorClass;
            String fallbackWrite;
            String tailWrite;
            if(inner.equals("int")){
                bytesConst="Integer.BYTES";
                speciesVar="FIXED_INT_SPECIES";
                vectorClass="IntVector";
                fallbackWrite="ByteIO.writeFixedIntArray("+bufVar+","+valuesVar+")";
                tailWrite="ByteIO.writeFixedInt("+bufVar+", "+valuesVar+"["+indexVar+"])";
            }else if(inner.equals("long")){
                bytesConst="Long.BYTES";
                speciesVar="FIXED_LONG_SPECIES";
                vectorClass="LongVector";
                fallbackWrite="ByteIO.writeFixedLongArray("+bufVar+","+valuesVar+")";
                tailWrite="ByteIO.writeFixedLong("+bufVar+", "+valuesVar+"["+indexVar+"])";
            }else if(inner.equals("float")){
                bytesConst="Float.BYTES";
                speciesVar="FIXED_FLOAT_SPECIES";
                vectorClass="FloatVector";
                fallbackWrite="ByteIO.writeFixedFloatArray("+bufVar+","+valuesVar+")";
                tailWrite="ByteIO.writeFloat("+bufVar+", "+valuesVar+"["+indexVar+"])";
            }else{
                bytesConst="Double.BYTES";
                speciesVar="FIXED_DOUBLE_SPECIES";
                vectorClass="DoubleVector";
                fallbackWrite="ByteIO.writeFixedDoubleArray("+bufVar+","+valuesVar+")";
                tailWrite="ByteIO.writeDouble("+bufVar+", "+valuesVar+"["+indexVar+"])";
            }
            sb.append(indent).append("{\n");
            sb.append(indent).append("    ").append(mapType(inner)).append("[] ").append(valuesVar).append("=").append(valueExpr).append(";\n");
            sb.append(indent).append("    int ").append(countVar).append("=").append(valuesVar).append("==null?0:").append(valuesVar).append(".length;\n");
            sb.append(indent).append("    ByteIO.writeSize(").append(bufVar).append(", ").append(countVar).append(");\n");
            sb.append(indent).append("    if(").append(countVar).append("!=0){\n");
            sb.append(indent).append("        if(").append(bufVar).append(" instanceof LinearByteBuffer || (").append(bufVar).append(" instanceof NettyCursor && ((NettyCursor)").append(bufVar).append(").unwrap().hasArray())){\n");
            sb.append(indent).append("            if(").append(bufVar).append(" instanceof LinearByteBuffer){ ((LinearByteBuffer)").append(bufVar).append(").ensureWritable(").append(countVar).append("*").append(bytesConst).append("); } else { ((NettyCursor)").append(bufVar).append(").unwrap().ensureWritable(").append(countVar).append("*").append(bytesConst).append("); }\n");
            sb.append(indent).append("            int ").append(offsetVar).append("=(").append(bufVar).append(" instanceof NettyCursor) ? ((NettyCursor)").append(bufVar).append(").unwrap().arrayOffset()+((NettyCursor)").append(bufVar).append(").unwrap().writerIndex() : ((LinearByteBuffer)").append(bufVar).append(").writerIndex();\n");
            sb.append(indent).append("            MemorySegment ").append(segmentVar).append("=MemorySegment.ofArray(").append(bufVar).append(".array());\n");
            sb.append(indent).append("            int ").append(boundVar).append("=").append(speciesVar).append(".loopBound(").append(countVar).append(");\n");
            sb.append(indent).append("            long ").append(byteOffsetVar).append("=").append(offsetVar).append(";\n");
            sb.append(indent).append("            for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(boundVar).append(";").append(indexVar).append("+=").append(speciesVar).append(".length()){\n");
            sb.append(indent).append("                ").append(vectorClass).append(".fromArray(").append(speciesVar).append(", ").append(valuesVar).append(", ").append(indexVar).append(").intoMemorySegment(").append(segmentVar).append(", ").append(byteOffsetVar).append(", ByteOrder.BIG_ENDIAN);\n");
            sb.append(indent).append("                ").append(byteOffsetVar).append(" += (long)").append(speciesVar).append(".length() * ").append(bytesConst).append(";\n");
            sb.append(indent).append("            }\n");
            sb.append(indent).append("            if(").append(bufVar).append(" instanceof NettyCursor){ ((NettyCursor)").append(bufVar).append(").unwrap().writerIndex(((NettyCursor)").append(bufVar).append(").unwrap().writerIndex()+").append(boundVar).append("*").append(bytesConst).append("); } else { ((LinearByteBuffer)").append(bufVar).append(").setWriterIndex(((LinearByteBuffer)").append(bufVar).append(").writerIndex()+").append(boundVar).append("*").append(bytesConst).append("); }\n");
            sb.append(indent).append("            for(int ").append(indexVar).append("=").append(boundVar).append(";").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            sb.append(indent).append("                ").append(tailWrite).append(";\n");
            sb.append(indent).append("            }\n");
            sb.append(indent).append("        }else{\n");
            sb.append(indent).append("            ").append(fallbackWrite).append(";\n");
            sb.append(indent).append("        }\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static String readFixedCursorValue(String bufVar, String t){
            if(t.equals("int")||t.equals("Integer")) return "ByteIO.readFixedInt("+bufVar+")";
            if(t.equals("long")||t.equals("Long")) return "ByteIO.readFixedLong("+bufVar+")";
            if(t.equals("byte")||t.equals("Byte")) return "ByteIO.readByte("+bufVar+")";
            if(t.equals("short")||t.equals("Short")) return "ByteIO.readFixedShort("+bufVar+")";
            if(t.equals("boolean")||t.equals("Boolean")) return "ByteIO.readBoolean("+bufVar+")";
            if(t.equals("char")||t.equals("Character")) return "ByteIO.readFixedChar("+bufVar+")";
            if(t.equals("float")||t.equals("Float")) return "ByteIO.readFloat("+bufVar+")";
            if(t.equals("double")||t.equals("Double")) return "ByteIO.readDouble("+bufVar+")";
            if(ENUMS.contains(t)) return t+".fromOrdinal(ByteIO.readFixedInt("+bufVar+"))";
            return readCursorValue(bufVar, t);
        }
        static String readFixedArrayValue(String bufVar, String t){
            String inner=t.substring(0, t.length()-2).trim();
            if(inner.equals("int") || inner.equals("Integer")) return "ByteIO.readFixedIntArray("+bufVar+")";
            if(inner.equals("long") || inner.equals("Long")) return "ByteIO.readFixedLongArray("+bufVar+")";
            if(inner.equals("byte") || inner.equals("Byte")) return "ByteIO.readBytes("+bufVar+")";
            if(inner.equals("short") || inner.equals("Short")) return "ByteIO.readFixedShortArray("+bufVar+")";
            if(inner.equals("boolean") || inner.equals("Boolean")) return "ByteIO.readBooleanArray("+bufVar+")";
            if(inner.equals("char") || inner.equals("Character")) return "ByteIO.readFixedCharArray("+bufVar+")";
            if(inner.equals("float") || inner.equals("Float")) return "ByteIO.readFixedFloatArray("+bufVar+")";
            if(inner.equals("double") || inner.equals("Double")) return "ByteIO.readFixedDoubleArray("+bufVar+")";
            String elemBuf=childVar(bufVar, "elem");
            return "ByteIO.readObjectArray("+bufVar+", "+javaObjectArrayCreatorExpr(t, "n")+", "+elemBuf+"->"+readFixedCursorValue(elemBuf, inner)+")";
        }
        static String writeFixedCursorValue(String bufVar, String valueExpr, String t){
            if(t.equals("int")||t.equals("Integer")) return "ByteIO.writeFixedInt("+bufVar+","+valueExpr+")";
            if(t.equals("long")||t.equals("Long")) return "ByteIO.writeFixedLong("+bufVar+","+valueExpr+")";
            if(t.equals("byte")||t.equals("Byte")) return "ByteIO.writeByte("+bufVar+","+valueExpr+")";
            if(t.equals("short")||t.equals("Short")) return "ByteIO.writeFixedShort("+bufVar+","+valueExpr+")";
            if(t.equals("boolean")||t.equals("Boolean")) return "ByteIO.writeBoolean("+bufVar+","+valueExpr+")";
            if(t.equals("char")||t.equals("Character")) return "ByteIO.writeFixedChar("+bufVar+","+valueExpr+")";
            if(t.equals("float")||t.equals("Float")) return "ByteIO.writeFloat("+bufVar+","+valueExpr+")";
            if(t.equals("double")||t.equals("Double")) return "ByteIO.writeDouble("+bufVar+","+valueExpr+")";
            if(ENUMS.contains(t)) return "ByteIO.writeFixedInt("+bufVar+","+(valueExpr)+"==null?0:"+valueExpr+".ordinal())";
            return writeCursorValue(bufVar, valueExpr, t);
        }
        static String writeFixedArrayValue(String bufVar, String valueExpr, String t){
            String inner=t.substring(0, t.length()-2).trim();
            if(inner.equals("int") || inner.equals("Integer")) return "ByteIO.writeFixedIntArray("+bufVar+","+valueExpr+")";
            if(inner.equals("long") || inner.equals("Long")) return "ByteIO.writeFixedLongArray("+bufVar+","+valueExpr+")";
            if(inner.equals("byte") || inner.equals("Byte")) return "ByteIO.writeBytes("+bufVar+","+valueExpr+")";
            if(inner.equals("short") || inner.equals("Short")) return "ByteIO.writeFixedShortArray("+bufVar+","+valueExpr+")";
            if(inner.equals("boolean") || inner.equals("Boolean")) return "ByteIO.writeBooleanArray("+bufVar+","+valueExpr+")";
            if(inner.equals("char") || inner.equals("Character")) return "ByteIO.writeFixedCharArray("+bufVar+","+valueExpr+")";
            if(inner.equals("float") || inner.equals("Float")) return "ByteIO.writeFixedFloatArray("+bufVar+","+valueExpr+")";
            if(inner.equals("double") || inner.equals("Double")) return "ByteIO.writeFixedDoubleArray("+bufVar+","+valueExpr+")";
            String elemBuf=childVar(bufVar, "elem");
            String elemVar=childVar(valueExpr, "elem");
            return "ByteIO.writeObjectArray("+bufVar+","+valueExpr+", ("+elemBuf+","+elemVar+")->"+writeFixedCursorValue(elemBuf, elemVar, inner)+")";
        }
        static boolean isHotExpandedType(String t){
            return (SIMD_ENABLED && (isPrimitiveByteArray(t) || isHotSimdRawPrimitiveArrayType(t)))
                    || isHotObjectArrayType(t)
                    || isListLikeType(t)
                    || isSetLikeType(t)
                    || isQueueLikeType(t)
                    || isMapLikeType(t);
        }
        static boolean isIntLikeType(String t){
            return "int".equals(t) || "Integer".equals(t);
        }
        static boolean isLongLikeType(String t){
            return "long".equals(t) || "Long".equals(t);
        }
        static boolean isPrimitiveSpecializedListType(String t){
            return isSpecializedIntListType(t) || isSpecializedLongListType(t);
        }
        static boolean isSpecializedIntListType(String t){
            if(!isListLikeType(t)) return false;
            String canonical=canonicalContainerType(t);
            if("LinkedList".equals(canonical) || "Collection".equals(canonical)) return false;
            return isIntLikeType(genericBody(t).trim());
        }
        static boolean isSpecializedLongListType(String t){
            if(!isListLikeType(t)) return false;
            String canonical=canonicalContainerType(t);
            if("LinkedList".equals(canonical) || "Collection".equals(canonical)) return false;
            return isLongLikeType(genericBody(t).trim());
        }
        static boolean isSpecializedIntIntMapType(String t){
            if(!isMapLikeType(t)) return false;
            String canonical=canonicalContainerType(t);
            if("LinkedHashMap".equals(canonical)) return false;
            List<String> kv=splitTopLevel(genericBody(t), ',');
            return kv.size()==2 && isIntLikeType(kv.get(0).trim()) && isIntLikeType(kv.get(1).trim());
        }
        static boolean isSpecializedIntLongMapType(String t){
            if(!isMapLikeType(t)) return false;
            String canonical=canonicalContainerType(t);
            if("LinkedHashMap".equals(canonical)) return false;
            List<String> kv=splitTopLevel(genericBody(t), ',');
            return kv.size()==2 && isIntLikeType(kv.get(0).trim()) && isLongLikeType(kv.get(1).trim());
        }
        static boolean isSpecializedIntObjectMapType(String t){
            if(!isMapLikeType(t)) return false;
            String canonical=canonicalContainerType(t);
            if("LinkedHashMap".equals(canonical)) return false;
            List<String> kv=splitTopLevel(genericBody(t), ',');
            return kv.size()==2
                    && isIntLikeType(kv.get(0).trim())
                    && !isIntLikeType(kv.get(1).trim())
                    && !isLongLikeType(kv.get(1).trim());
        }
        static Struct structDef(String t){
            return STRUCTS.get(t);
        }
        static boolean isStructType(String t){
            return STRUCTS.containsKey(t);
        }
        static boolean isInlineStructType(String t){
            Struct struct=structDef(t);
            return struct!=null && (struct.inline || struct.fields.size()<=4);
        }
        static boolean isFixedStruct(Struct s){
            return s!=null && s.fixed && isFixedCompatibleStruct(s, new java.util.HashSet<>());
        }
        static boolean isFixedCompatibleType(String t){
            return isFixedCompatibleType(t, new java.util.HashSet<>());
        }
        static boolean isFixedCompatibleType(String t, java.util.Set<String> visiting){
            if(t.endsWith("[]")){
                return isFixedWidthArrayType(t, visiting);
            }
            if(isPrimitive(t) || ENUMS.contains(t)) return true;
            if(t.equals("Integer") || t.equals("Long") || t.equals("Byte") || t.equals("Short")
                    || t.equals("Boolean") || t.equals("Character") || t.equals("Float") || t.equals("Double")){
                return true;
            }
            if(isContainerType(t) || t.endsWith("[]") || t.equals("String") || isOptionalType(t)){
                return false;
            }
            Struct nested=structDef(t);
            return nested!=null && isFixedCompatibleStruct(nested, visiting);
        }
        static boolean isFixedWidthArrayType(String t, java.util.Set<String> visiting){
            String inner=t.substring(0, t.length()-2).trim();
            if(isPrimitive(inner) || ENUMS.contains(inner)){
                return true;
            }
            Struct nested=structDef(inner);
            return nested!=null && isFixedCompatibleStruct(nested, visiting);
        }
        static boolean isFixedCompatibleStruct(Struct s, java.util.Set<String> visiting){
            if(s==null) return false;
            if(!s.fixed) return false;
            if(!visiting.add(s.name)) return true;
            try{
                for(Field field: s.fields){
                    if(isOptionalType(field.type) || !isFixedCompatibleType(field.type, visiting)){
                        return false;
                    }
                }
                return true;
            }finally{
                visiting.remove(s.name);
            }
        }
        static boolean isPrimitiveByteArray(String t){
            return t.equals("byte[]") || t.equals("Byte[]");
        }
        static boolean isHotSimdRawPrimitiveArrayType(String t){
            if(!t.endsWith("[]")){
                return false;
            }
            String inner=t.substring(0, t.length()-2).trim();
            return inner.equals("int") || inner.equals("long") || inner.equals("float") || inner.equals("double");
        }
        static boolean isFixedSimdPrimitiveArray(String t){
            if(!t.endsWith("[]")){
                return false;
            }
            String inner=t.substring(0, t.length()-2).trim();
            return inner.equals("int") || inner.equals("long") || inner.equals("float") || inner.equals("double");
        }
        static boolean hasFixedSimdArrayField(List<Field> fields){
            for(Field field: fields){
                if(isFixedSimdPrimitiveArray(field.type)){
                    return true;
                }
            }
            return false;
        }
        static void appendJavaSIMDReadByteArray(StringBuilder sb, String targetExpr, String t, String bufVar, String indent){
            String countVar=childVar(targetExpr, "count");
            String tmpVar=childVar(targetExpr, "tmp");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    byte[] ").append(tmpVar).append("=new byte[").append(countVar).append("];\n");
            sb.append(indent).append("    ByteIO.readRawByteArray(").append(bufVar).append(", ").append(tmpVar).append(", ").append(countVar).append(");\n");
            sb.append(indent).append("    ").append(targetExpr).append("=").append(tmpVar).append(";\n");
            sb.append(indent).append("}\n");
        }
        static boolean isHotObjectArrayType(String t){
            return t.endsWith("[]") && !isPrimitive(t.substring(0, t.length()-2).trim());
        }
        static boolean hasPrimitiveArrayField(List<Field> fields){
            for(Field f: fields){
                String t=f.type;
                if(t.endsWith("[]")){
                    String inner=t.substring(0, t.length()-2).trim();
                    if(isPrimitive(inner)){
                        return true;
                    }
                }
            }
            return false;
        }
        static String generateBoImpl(String protoPkg, String boPkg, String base, Proto p, boolean withComponent){
            // 濠电姷鏁告慨鐑藉极閸涘﹥鍙忛柣鎴ｆ閺嬩線鏌涘☉姗堟敾闁告瑥绻橀弻锝夊箣濠垫劖缍楅梺閫炲苯澧柛濠傛健楠炴劖绻濋崘顏嗗骄闂佸啿鎼鍥╃矓椤旈敮鍋撶憴鍕８闁告梹鍨甸锝夊醇閺囩偟顓洪梺缁樼懃閹虫劙鐛姀銈嗏拻闁稿本鐟чˇ锕傛煙濞村澧茬紒妤冨枎铻栭柛娑卞幘閻撴垿鏌熼崗鑲╂殬闁告柨绉瑰畷鎴﹀礋椤栨稓鍘遍梺鏂ユ櫅閸橀箖鎳栭埡鍌氬簥闂佺硶鍓濊彠濞存粍绮撻弻鈥愁吋閸愩劌顬夐梺姹囧妽閸ㄥ爼骞堥妸鈺傛櫜闁搞儜鍌涱潟闂備礁鎼張顒傜矙閹捐鐒垫い鎺戯功缁夌敻鏌涚€ｎ亝鍣藉ù婊勬倐椤㈡﹢鎮㈢紙鐘电泿婵＄偑鍊栭崝褏寰婄捄銊т笉闁绘劗鍎ら悡娆愩亜閺冨倹鍤€濠⒀勭叀閺岀喖顢涘☉娆樻闂佺硶鏅粻鎾诲春閳ь剚銇勯幒鎴濐仼缂佺媭鍨遍妵鍕箛閸洘顎嶉梺缁樻尵閸犳牠鐛弽顬ュ酣顢楅埀顒勫焵椤戞儳鈧洟鈥﹂崶顒€绠涙い鎾跺Х椤旀洟姊洪崨濠勬噧妞わ箒浜划濠氭倷閻戞鍙嗗┑鐘绘涧閻楀棙绂掗敂閿亾閸偅绶查悗姘嵆閻涱噣宕堕澶嬫櫌闂佺鏈划宥呅掓惔銊︹拻闁稿本鐟чˇ锕傛煙绾板崬浜扮€规洦鍨堕、鏇㈡晜閽樺缃曢梻浣虹《閸撴繈鏁嬮梺鍛婃⒐濡啫顫忔繝姘＜婵炲棙鍨垫俊浠嬫煟鎼达絿鎳楅柛鎰亾缂嶅酣鎮峰鍛暭閻㈩垱甯炴竟鏇犳崉閵娿垹浜鹃悷娆忓缁€鈧┑鐐额嚋缁犳挸顕ｉ崘宸叆闁割偅绻勯鎰攽閻戝洨绉甸柛鎾寸懄娣囧﹥绂掔€ｎ偆鍘介梺瑙勫礃濞夋盯寮稿☉娆樻闁绘劕顕晶顒佺箾閻撳海绠荤€规洘绮忛ˇ鎾煥濞戞艾鏋涙慨濠勫劋鐎电厧鈻庨幋鐘橈綁姊洪崨濠勬噧闁哥喐娼欓锝囨嫚濞村顫嶅┑鐐叉閸旀洟宕濋崨瀛樷拺闂傚牊渚楅悞楣冩煕婵犲啰澧电€规洘婢橀～婵嬵敄閳哄倹顥堥柟顔规櫊濡啫鈽夊Δ鍐╁礋缂傚倸鍊烽懗鍓佸垝椤栨粍鏆滈柨鐔哄Т閺勩儵鏌嶈閸撴岸濡甸崟顖氱闁规惌鍨版慨娑氱磽娴ｅ壊妲洪柡浣割煼瀵鈽夐姀鈥充汗閻庤娲栧ú銈夊煕瀹€鍕拺閻犲洠鈧櫕鐏堝┑鐐点€嬬换婵嬪Υ娴ｅ壊娼╅悹楦挎閸旓箑顪冮妶鍡楃瑨閻庢凹鍓熼幏鎴︽偄閸濄儳顔曢梺鐟扮摠閻熴儵鎮橀埡鍛埞妞ゆ牗鍑瑰〒濠氭煏閸繃顥為柍閿嬪浮閺屾稑螣閻樺弶绁紓宥嗙墬閵囧嫯绠涢幘璺侯杸闂佹娊鏀遍崹鍧楀蓟閻旂厧绠氶柡澶婃櫇閹剧粯鐓涘〒姘ｅ亾濞存粌鐖煎璇测槈閵忕姈鈺呮煏婢舵稓鐣卞ù鐘虫尦閹鈻撻崹顔界亪濡炪値鍘鹃崗姗€鐛崘顔碱潊闁靛牆妫欓崕顏堟⒑闂堚晛鐦滈柛娆忕箳濡叉劙宕ｆ径宀€鐦堢紒鍓у钃辨い顐躬閺屾盯濡搁敃鈧埢鏇犫偓瑙勬礃濞茬喐淇婇崼鏇炵倞闁靛鍎宠ぐ鎾⒒娴ｈ櫣甯涢柛鏃€顨婂畷鏇㈠Χ婢跺﹦鍘遍梺鐟邦嚟婵澹曢挊澹濆綊鏁愰崼顐㈡異闂佺粯甯婄划娆撳蓟瀹ュ鏁嶆繛鎴炵懅椤︻厾绱撴担浠嬪摵閻㈩垽绻濋妴浣糕枎閹惧磭顦ч梺绋跨箳閸樠囨⒒椤栨稓绡€缁剧増菤閸嬫捇宕橀懠顒勭崜闂備礁鎲″褰掓偡閳哄懏鍋樻い鏇楀亾妤犵偞甯￠獮濠囨惞椤愶綆妫冮梺绯曟杹閸嬫挸顪冮妶鍡楃瑨閻庢凹鍙冨畷鏇炍旀担椋庣畾闂侀潧鐗嗙€氼參藝妞嬪海纾奸悹鍥у级椤ョ偤鏌曢崶褍顏€殿喕绮欐俊姝岊檨闁哄棴绻濆铏规嫚閳ュ磭浠╅梺缁橆殔缁绘帒危閹版澘绠抽柟鎯у閹虫繈姊洪幖鐐插妧闁告洦鍘肩紞鍡涙⒒閸屾瑦绁版い鏇熺墵瀹曟澘螖閸涱偀鍋撻崘顔奸唶闁靛鍎抽悿鍛存⒑閸︻叀妾搁柛鐘崇墱缁牏鈧綆鍋佹禍婊堟煙閻戞ê鐏ュù婊呭仦娣囧﹪鎳犻鈧。鑲╃磼缂佹绠橀柛鐘诧攻瀵板嫬鐣濋埀顒勬晬閻斿吋鈷戠紒瀣儥閸庢劖銇勯鐐村枠鐎规洘宀搁獮鎺楀箣閺冣偓閻庡姊虹憴鍕婵炲绋撶划濠囨晝閸屾稈鎷洪梺鍛婄箓鐎氼噣鍩㈡径鎰厱婵☆垱浜介崑銏☆殽閻愭潙鐏撮柟铏矒閹瑩鏌呭☉姘辨晨闂傚倷娴囬～澶婄暦濡　鏋栨繛鎴欏灩閸戠娀骞栧ǎ顒€濡介柣鎾跺枑缁绘繈妫冨☉娆忔閻庤鎸稿Λ娆撳箞閵婏妇绡€闁告劏鏂傛禒銏ゆ倵濞堝灝娅橀柛鎾跺枑娣囧﹪鎮滈懞銉︽珕闂佷紮绲介懟顖滃緤娴犲鈷掗柛灞剧懅椤︼箓鏌熼懞銉х煉鐎规洘濞婃俊鐑藉煛娴ｅ摜鈧參鏌ｉ悩鐑樸€冮悹鈧敃鍌氬惞闁哄洢鍨洪崐鐢告煕閿旇骞栭崯鎼佹⒑濮瑰洤鈧劙宕戦幘缁樷拻濞达絽鎲＄拹锟犳煣韫囨捇鍙勭€规洖缍婇弻鍡楊吋閸涱噮妫熼梻渚€鈧偛鑻晶瀛樻叏婵犲嫮甯涢柟宄版嚇閹煎綊鎮烽幍顕呭仹闂傚倷绀侀幉鈥愁潖閻熸噴娲冀椤掑倷鑸繝鐢靛Х閺佸憡鎱ㄩ弶鎳ㄦ椽濡舵径濠呅曢悷婊呭鐢鎮￠悢鍏肩厸闁稿本绻冪涵鑸电箾閸儰鎲鹃柡宀嬬節閸┾偓妞ゆ帒瀚崵宥夋煏婢舵稓瀵肩紒銊ヮ煼濮婃椽宕崟顓夌娀鏌涢弬璺ㄧ劯鐎规洜鏁婚、妤呭礋椤掑倸骞堥梻渚€娼ч悧鍡椕洪妶澶婂嚑闁哄啫鐗婇悡鍐喐濠婂牆绀堟繛鍡樻尰閸婅埖鎱ㄥ鍡楀⒒闁绘柨妫欓幈銊ヮ渻鐠囪弓澹曢梻浣芥〃缁€渚€宕幘顔衡偓渚€寮崼婵堫槹濡炪倖鎸鹃崰鎰邦敊閺囩姷纾介柛灞剧懅椤︼附銇勯幋婵堝ⅵ妞ゃ垺宀搁獮搴ㄦ寠婢跺瞼娼夐梻渚€鈧偛鑻晶瀛橆殽閻愯尙绠荤€规洏鍔庨埀顒佺⊕鑿ら柟宄扮秺濮婇缚銇愰幒鎴滃枈闂佸憡鎸婚悷銉暰?00闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕偓浠嬫晸閵夆晛纾婚柕蹇嬪€栭悡鏇㈡煟閹邦垰鐨洪柛鈺嬬稻閹便劍绻濋崘鈹夸虎濠碘槅鍋勯崯顐﹀煡婢跺缍囬柕濞垮灪閻忎線姊婚崒娆戭槮闁硅姤绮嶉幈銊╂偨閹肩偐鍋撻崘鈺冪瘈闁稿被鍊曞▓?+ 婵犵數濮烽弫鍛婃叏閻戣棄鏋侀柛娑橈攻閸欏繘鏌ｉ幋锝嗩棄闁哄绶氶弻娑樷槈濮楀牊鏁鹃梺鍛婄懃缁绘﹢寮婚敐澶婎潊闁绘ê妯婂Λ宀勬⒑鏉炴壆顦﹂柨鏇ㄤ邯瀵鍨鹃幇浣告倯闁硅偐琛ラ埀顒€纾鎰版⒒閸屾艾鈧悂宕戦崱娑樺瀭闂侇剙绉存闂佸憡娲﹂崹浼村礃閳ь剟姊洪棃娴ゆ盯宕ㄩ姘瑢缂傚倸鍊搁崐宄懊归崶鈺冪濞村吋娼欑壕瑙勭節闂堟侗鍎忛柦鍐枛閺屻劌鈹戦崱鈺傂ч梺鍝勬噺閻擄繝寮诲☉妯锋闁告鍋為悘宥夋⒑閸︻厼鍘村ù婊冪埣楠炲啫螖閸愨晛鏋傞梺鍛婃处閸撴盯藝閵娾晜鈷戠紓浣股戦幆鍫㈢磼缂佹绠為柣娑卞櫍瀹曟﹢濡告惔銏☆棃鐎规洏鍔戦、娆撴嚍閵壯冪闂傚倷鑳堕、濠囧磻閹邦喗鍋橀柕澶嗘櫅缁€鍫熺節闂堟侗鍎愰柛濠傚閳ь剙绠嶉崕閬嵥囨导鏉戠厱闁瑰濮风壕钘壝归敐鍫濅簵闁瑰濮抽悞濠冦亜閹惧崬鐏柣鎾存礀閳规垿鎮╅幓鎺嗗亾閸︻厽瀚婚柨鐔哄У閻撴瑦顨ラ悙鑼虎闁诲繆鏅犻弻宥囨喆閸曨偆浼岄悗瑙勬礀閻栧ジ宕洪敓鐘茬妞ゅ繐鎷嬪鎾绘⒒閸屾艾鈧兘鎳楅崼鏇椻偓锕傚醇閵夈儱鐝樺銈嗗笒閸婃悂宕瑰┑鍫氬亾閸忓浜鹃梺鍛婃磵閺備線宕戦幘璇茬＜闁绘劘寮撶槐鍫曟⒑閸涘﹥纾搁柛鏂跨Ч瀵剟鍩€椤掑嫭鈷掑ù锝呮憸閺嬪啯淇婇銏狀仼閾荤偞淇婇妶鍕厡妞も晛寮剁换婵嬫濞戝崬鍓遍梺缁樻尪閸庣敻寮婚敓鐘茬倞妞ゎ厼顑愭禍顏堝箖濮椻偓瀹曪絾寰勫Ο娲诲晬闂備胶绮崝鏍亹閸愵喖姹叉繛鍡樻尰閻撶喖鏌ㄥ┑鍡欑缂佲檧鍋撴俊銈囧Х閸嬫稓绮旇ぐ鎺戠鐟滅増甯╅弫鍐煏閸繂鈧憡绂嶉幆褉鏀介柣妯虹－椤ｅ弶銇勯妷銉敾闁靛洤瀚伴獮鍥煛娴ｈ桨鐥┑鐘灱濞夋稓鈧矮鍗冲濠氭偄鏉炴壆鍓ㄩ梺鍝勭Р閸斿秹鎮甸弴銏＄厽閹兼番鍔嶅☉褔鏌曢崼鐔稿€愮€规洘妞介崺鈧い鎺嶉檷娴滄粓鏌熼悜妯虹仴妞ゅ浚浜弻宥夋煥鐎ｎ亞鐟ㄩ梺闈涙鐢帡锝炲┑瀣櫜闁告侗鍓欓ˉ姘攽閻樺灚鏆╅柛瀣耿瀹曠娀鎮╃拠鑼槯闂佺粯鍔﹂崜娑㈠煡婢舵劖鎳氶柡宥庡幗閻撴洘绻涢幋婵嗚埞闁哄鍠愮换娑㈠川椤撶喎鏋犲┑顔硷攻濡炰粙鐛幇顓熷劅闁冲灈鏅滅€氫粙姊绘担渚劸妞ゆ垵妫濋獮鎴﹀炊椤掍焦娅?00闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕偓浠嬫晸閵夆晛纾婚柕蹇嬪€栭悡鏇㈡煟閹邦垰鐨洪柛鈺嬬稻閹便劍绻濋崘鈹夸虎濠碘槅鍋勯崯顐﹀煡婢跺缍囬柕濞垮灪閻忎線姊婚崒娆戭槮闁硅姤绮嶉幈銊╂偨閹肩偐鍋撻崘鈺冪瘈闁稿被鍊曞▓?
            int estimatedSize=800+p.c2s.size()*200;
            StringBuilder sb=new StringBuilder(estimatedSize);
            sb.append("package ").append(boPkg).append(".impl;\n\n");
            if(withComponent){
                sb.append("import org.springframework.stereotype.Component;\n");
            }
            sb.append("import io.netty.channel.Channel;\n");
            sb.append("import java.util.*;\n");
            sb.append("import ").append(protoPkg).append(".*;\n");
            sb.append("import ").append(boPkg).append(".I").append(base).append("BO;\n\n");
            if(withComponent){
                sb.append("@Component\n");
            }
            sb.append("public class ").append(base).append("BOImp implements I").append(base).append("BO {\n");
            for(Method m: p.c2s){
                sb.append("    @Override public void ").append(m.name).append("(Channel channel");
                for(Field f: m.params){
                    sb.append(", ").append(mapType(f.type)).append(" ").append(f.name);
                }
                sb.append("){\n");
                sb.append("        // TODO: add BO implementation logic when needed.\n");
                sb.append("    }\n");
            }
            sb.append("}\n");
            return sb.toString();
        }
        static String generateAutoConfig(String protoPkg, String boPkg, List<Assign> assigns, boolean scanImpl){
            // 濠电姷鏁告慨鐑藉极閸涘﹥鍙忛柣鎴ｆ閺嬩線鏌涘☉姗堟敾闁告瑥绻橀弻锝夊箣濠垫劖缍楅梺閫炲苯澧柛濠傛健楠炴劖绻濋崘顏嗗骄闂佸啿鎼鍥╃矓椤旈敮鍋撶憴鍕８闁告梹鍨甸锝夊醇閺囩偟顓洪梺缁樼懃閹虫劙鐛姀銈嗏拻闁稿本鐟чˇ锕傛煙濞村澧茬紒妤冨枎铻栭柛娑卞幘閻撴垿鏌熼崗鑲╂殬闁告柨绉瑰畷鎴﹀礋椤栨稓鍘遍梺鏂ユ櫅閸橀箖鎳栭埡鍌氬簥闂佺硶鍓濊彠濞存粍绮撻弻鈥愁吋閸愩劌顬夐梺姹囧妽閸ㄥ爼骞堥妸鈺傛櫜闁搞儜鍌涱潟闂備礁鎼張顒傜矙閹捐鐒垫い鎺戯功缁夌敻鏌涚€ｎ亝鍣藉ù婊勬倐椤㈡﹢鎮㈢紙鐘电泿婵＄偑鍊栭崝褏寰婄捄銊т笉闁绘劗鍎ら悡娆愩亜閺冨倹鍤€濠⒀勭叀閺岀喖顢涘☉娆樻闂佺硶鏅粻鎾诲春閳ь剚銇勯幒鎴濐仼缂佺媭鍨遍妵鍕箛閸洘顎嶉梺缁樻尵閸犳牠鐛弽顬ュ酣顢楅埀顒勫焵椤戞儳鈧洟鈥﹂崶顒€绠涙い鎾跺Х椤旀洟姊洪崨濠勬噧妞わ箒浜划濠氭倷閻戞鍙嗗┑鐘绘涧閻楀棙绂掗敂閿亾閸偅绶查悗姘嵆閻涱噣宕堕澶嬫櫌闂佺鏈划宥呅掓惔銊︹拻闁稿本鐟чˇ锕傛煙绾板崬浜扮€规洦鍨堕、鏇㈡晜閽樺缃曢梻浣虹《閸撴繈鏁嬮梺鍛婃⒐濡啫顫忔繝姘＜婵炲棙鍨垫俊浠嬫煟鎼达絿鎳楅柛鎰亾缂嶅酣鎮峰鍛暭閻㈩垱甯炴竟鏇犳崉閵娿垹浜鹃悷娆忓缁€鈧┑鐐额嚋缁犳挸顕ｉ崘宸叆闁割偅绻勯鎰攽閻戝洨绉甸柛鎾寸懄娣囧﹥绂掔€ｎ偆鍘介梺瑙勫礃濞夋盯寮稿☉娆樻闁绘劕顕晶顒佺箾閻撳海绠荤€规洘绮忛ˇ鎾煥濞戞艾鏋涙慨濠勫劋鐎电厧鈻庨幋鐘橈綁姊洪崨濠勬噧闁哥喐娼欓锝囨嫚濞村顫嶅┑鐐叉閸旀洟宕濋崨瀛樷拺闂傚牊渚楅悞楣冩煕婵犲啰澧电€规洘婢橀～婵嬵敄閳哄倹顥堥柟顔规櫊濡啫鈽夊Δ鍐╁礋缂傚倸鍊烽懗鍓佸垝椤栨粍鏆滈柨鐔哄Т閺勩儵鏌嶈閸撴岸濡甸崟顖氱闁规惌鍨版慨娑氱磽娴ｅ壊妲洪柡浣割煼瀵鈽夐姀鈥充汗閻庤娲栧ú銈夊煕瀹€鍕拺閻犲洠鈧櫕鐏堝┑鐐点€嬬换婵嬪Υ娴ｅ壊娼╅悹楦挎閸旓箑顪冮妶鍡楃瑨閻庢凹鍓熼幏鎴︽偄閸濄儳顔曢梺鐟扮摠閻熴儵鎮橀埡鍛埞妞ゆ牗鍑瑰〒濠氭煏閸繃顥為柍閿嬪浮閺屾稑螣閻樺弶绁紓宥嗙墬閵囧嫯绠涢幘璺侯杸闂佹娊鏀遍崹鍧楀蓟閻旂厧绠氶柡澶婃櫇閹剧粯鐓涘〒姘ｅ亾濞存粌鐖煎璇测槈閵忕姈鈺呮煏婢舵稓鐣卞ù鐘虫尦閹鈻撻崹顔界亪濡炪値鍘鹃崗姗€鐛崘顔碱潊闁靛牆妫欓崕顏堟⒑闂堚晛鐦滈柛娆忕箳濡叉劙宕ｆ径宀€鐦堢紒鍓у钃辨い顐躬閺屾盯濡搁敃鈧埢鏇犫偓瑙勬礃濞茬喐淇婇崼鏇炵倞闁靛鍎宠ぐ鎾⒒娴ｈ櫣甯涢柛鏃€顨婂畷鏇㈠Χ婢跺﹦鍘遍梺鐟邦嚟婵澹曢挊澹濆綊鏁愰崼顐㈡異闂佺粯甯婄划娆撳蓟瀹ュ鏁嶆繛鎴炵懅椤︻厾绱撴担浠嬪摵閻㈩垽绻濋妴浣糕枎閹惧磭顦ч梺绋跨箳閸樠囨⒒椤栨稓绡€缁剧増菤閸嬫捇宕橀懠顒勭崜闂備礁鎲″褰掓偡閳哄懏鍋樻い鏇楀亾妤犵偞甯￠獮濠囨惞椤愶綆妫冮梺绯曟杹閸嬫挸顪冮妶鍡楃瑨閻庢凹鍙冨畷鏇炍旀担椋庣畾闂侀潧鐗嗙€氼參藝妞嬪海纾奸悹鍥у级椤ョ偤鏌曢崶褍顏€殿喕绮欐俊姝岊檨闁哄棴绻濆铏规嫚閳ュ磭浠╅梺缁橆殔缁绘帒危閹版澘绠抽柟鎯у閹虫繈姊洪幖鐐插妧闁告洦鍘肩紞鍡涙⒒閸屾瑦绁版い鏇熺墵瀹曟澘螖閸涱偀鍋撻崘顔奸唶闁靛鍎抽悿鍛存⒑閸︻叀妾搁柛鐘崇墱缁牏鈧綆鍋佹禍婊堟煙閻戞ê鐏ュù婊呭仦娣囧﹪鎳犻鈧。鑲╃磼缂佹绠橀柛鐘诧攻瀵板嫬鐣濋埀顒勬晬閻斿吋鈷戠紒瀣儥閸庢劖銇勯鐐村枠鐎规洘宀搁獮鎺楀箣閺冣偓閻庡姊虹憴鍕婵炲绋撶划濠囨晝閸屾稈鎷洪梺鍛婄箓鐎氼噣鍩㈡径鎰厱婵☆垱浜介崑銏☆殽閻愭潙鐏撮柟铏矒閹瑩鏌呭☉姘辨晨闂傚倷娴囬～澶婄暦濡　鏋栨繛鎴欏灩閸戠娀骞栧ǎ顒€濡介柣鎾跺枑缁绘繈妫冨☉娆忔閻庤鎸稿Λ娆撳箞閵婏妇绡€闁告劏鏂傛禒銏ゆ倵濞堝灝娅橀柛鎾跺枑娣囧﹪鎮滈懞銉︽珕闂佷紮绲介懟顖滃緤娴犲鈷掗柛灞剧懅椤︼箓鏌熼懞銉х煉鐎规洘濞婃俊鐑藉煛娴ｅ摜鈧參鏌ｉ悩鐑樸€冮悹鈧敃鍌氬惞闁哄洢鍨洪崐鐢告煕閿旇骞栭崯鎼佹⒑濮瑰洤鈧劙宕戦幘缁樷拻濞达絽鎲＄拹锟犳煣韫囨捇鍙勭€规洖缍婇弻鍡楊吋閸涱噮妫熼梻渚€鈧偛鑻晶瀛樻叏婵犲嫮甯涢柟宄版嚇閹煎綊鎮烽幍顕呭仹闂傚倷绀侀幉鈥愁潖閻熸噴娲冀椤掑倷鑸繝鐢靛Х閺佸憡鎱ㄩ弶鎳ㄦ椽濡舵径濠呅曢悷婊呭鐢鎮￠悢鍏肩厸闁稿本绻冪涵鑸电箾閸儰鎲鹃柡宀嬬節閸┾偓妞ゆ帒瀚崵宥夋煏婢舵稓瀵肩紒銊ヮ煼濮婃椽宕崟顓夌娀鏌涢弬璺ㄧ劯鐎规洜鏁婚、妤呭礋椤掑倸骞堥梻渚€娼ч悧鍡椕洪妶澶婂嚑闁哄啫鐗婇悡鍐喐濠婂牆绀堟繛鍡樻尰閸婅埖鎱ㄥ鍡楀⒒闁绘柨妫欓幈銊ヮ渻鐠囪弓澹曢梻浣芥〃缁€渚€宕幘顔衡偓渚€寮崼婵堫槹濡炪倖鎸鹃崰鎰邦敊閺囩姷纾介柛灞剧懅椤︼附銇勯幋婵堝ⅵ妞ゃ垺宀搁獮搴ㄦ寠婢跺瞼娼夐梻渚€鈧偛鑻晶瀛橆殽閻愯尙绠荤€规洏鍔庨埀顒佺⊕鑿ら柟宄扮秺濮婇缚銇愰幒鎴滃枈闂佸憡鎸婚悷銉暰?00闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕偓浠嬫晸閵夆晛纾婚柕蹇嬪€栭悡鏇㈡煟閹邦垰鐨洪柛鈺嬬稻閹便劍绻濋崘鈹夸虎濠碘槅鍋勯崯顐﹀煡婢跺缍囬柕濞垮灪閻忎線姊婚崒娆戭槮闁硅姤绮嶉幈銊╂偨閹肩偐鍋撻崘鈺冪瘈闁稿被鍊曞▓?+ 婵犵數濮烽弫鍛婃叏閻戣棄鏋侀柛娑橈攻閸欏繘鏌ｉ幋锝嗩棄闁哄绶氶弻娑樷槈濮楀牊鏁鹃梺鍛婄懃缁绘﹢寮婚敐澶婎潊闁绘ê妯婂Λ宀勬⒑鏉炴壆顦﹂柨鏇ㄤ邯瀵鍨鹃幇浣告倯闁硅偐琛ラ埀顒€纾鎰版⒒閸屾艾鈧悂宕戦崱娑樺瀭闂侇剙绉存闂佸憡娲﹂崹浼村礃閳ь剟姊洪棃娴ゆ盯宕ㄩ姘瑢缂傚倸鍊搁崐宄懊归崶鈺冪濞村吋娼欑壕瑙勭節闂堟侗鍎忛柦鍐枛閺屻劌鈹戦崱鈺傂ч梺鍝勬噺閻擄繝寮诲☉妯锋闁告鍋為悘宥夋⒑閸︻厼鍘村ù婊冪埣楠炲啫螖閸愨晛鏋傞梺鍛婃处閸撴盯藝閵娾晜鈷戠紓浣股戦幆鍫㈢磼缂佹绠為柣娑卞櫍瀹曟﹢濡告惔銏☆棃鐎规洏鍔戦、娆撴嚍閵壯冪闂傚倷鑳堕、濠囧磻閹邦喗鍋橀柕澶嗘櫅缁€鍫熺節闂堟侗鍎愰柛濠傚閳ь剙绠嶉崕閬嵥囨导鏉戠厱闁瑰濮风壕钘壝归敐鍫濅簵闁瑰濮抽悞濠冦亜閹惧崬鐏柣鎾存礀閳规垿鎮╅幓鎺嗗亾閸︻厽瀚婚柨鐔哄У閻撴瑦顨ラ悙鑼虎闁诲繆鏅犻弻宥夋寠婢舵ɑ鈻堟繝娈垮枓閸嬫捇姊虹紒妯曟垿顢欓弽顓炵柈閻庯綆鍠楅埛鎺懨归敐鍛暈閻犳劧绱曠槐鎺楊敋閸涱厾浠梺杞扮贰閸ｏ綁宕洪埄鍐懝闁搞儮鏂侀崑鎾诲醇閺囩喓鍘介梺鎸庣箓閹冲繘顢撳畡鐖婄紓?50闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕偓浠嬫晸閵夆晛纾婚柕蹇嬪€栭悡鏇㈡煟閹邦垰鐨洪柛鈺嬬稻閹便劍绻濋崘鈹夸虎濠碘槅鍋勯崯顐﹀煡婢跺缍囬柕濞垮灪閻忎線姊婚崒娆戭槮闁硅姤绮嶉幈銊╂偨閹肩偐鍋撻崘鈺冪瘈闁稿被鍊曞▓?
            StringBuilder sb=new StringBuilder(800+assigns.size()*150);
            sb.append("package ").append(boPkg).append(".config;\n\n");
            sb.append("import org.springframework.context.annotation.*;\n");
            sb.append("import org.springframework.beans.factory.ObjectProvider;\n");
            sb.append("import ").append(boPkg).append(".*;\n");
            sb.append("import ").append(boPkg).append(".ProtoDispatchManager;\n\n");
            sb.append("@Configuration\n");
            if(scanImpl){
                sb.append("@ComponentScan(basePackages = \"").append(boPkg).append(".impl\")\n");
            }
            sb.append("public class GeneratedProtoAutoConfig {\n");
            sb.append("    @Bean(name = \"protoDispatchManager\")\n");
            sb.append("    public ProtoDispatchManager protoDispatchManager(");
            for(int i=0;i<assigns.size();i++){
                Assign a=assigns.get(i);
                if(i>0) sb.append(", ");
                sb.append("ObjectProvider<I").append(a.baseCamel).append("BO> ").append(lowerFirst(a.baseCamel));
            }
            sb.append("){\n");
            sb.append("        ProtoDispatchManager mgr = new ProtoDispatchManager();\n");
            for(Assign a: assigns){
                sb.append("        ").append(lowerFirst(a.baseCamel)).append(".ifAvailable(mgr::register").append(a.baseCamel).append(");\n");
            }
            sb.append("        return mgr;\n");
            sb.append("    }\n");
            sb.append("}\n");
            return sb.toString();
        }
        static String generateIds(String pkg, List<Assign> assigns){
            // 濠电姷鏁告慨鐑藉极閸涘﹥鍙忛柣鎴ｆ閺嬩線鏌涘☉姗堟敾闁告瑥绻橀弻锝夊箣濠垫劖缍楅梺閫炲苯澧柛濠傛健楠炴劖绻濋崘顏嗗骄闂佸啿鎼鍥╃矓椤旈敮鍋撶憴鍕８闁告梹鍨甸锝夊醇閺囩偟顓洪梺缁樼懃閹虫劙鐛姀銈嗏拻闁稿本鐟чˇ锕傛煙濞村澧茬紒妤冨枎铻栭柛娑卞幘閻撴垿鏌熼崗鑲╂殬闁告柨绉瑰畷鎴﹀礋椤栨稓鍘遍梺鏂ユ櫅閸橀箖鎳栭埡鍌氬簥闂佺硶鍓濊彠濞存粍绮撻弻鈥愁吋閸愩劌顬夐梺姹囧妽閸ㄥ爼骞堥妸鈺傛櫜闁搞儜鍌涱潟闂備礁鎼張顒傜矙閹捐鐒垫い鎺戯功缁夌敻鏌涚€ｎ亝鍣藉ù婊勬倐椤㈡﹢鎮㈢紙鐘电泿婵＄偑鍊栭崝褏寰婄捄銊т笉闁绘劗鍎ら悡娆愩亜閺冨倹鍤€濠⒀勭叀閺岀喖顢涘☉娆樻闂佺硶鏅粻鎾诲春閳ь剚銇勯幒鎴濐仼缂佺媭鍨遍妵鍕箛閸洘顎嶉梺缁樻尵閸犳牠鐛弽顬ュ酣顢楅埀顒勫焵椤戞儳鈧洟鈥﹂崶顒€绠涙い鎾跺Х椤旀洟姊洪崨濠勬噧妞わ箒浜划濠氭倷閻戞鍙嗗┑鐘绘涧閻楀棙绂掗敂閿亾閸偅绶查悗姘嵆閻涱噣宕堕澶嬫櫌闂佺鏈划宥呅掓惔銊︹拻闁稿本鐟чˇ锕傛煙绾板崬浜扮€规洦鍨堕、鏇㈡晜閽樺缃曢梻浣虹《閸撴繈鏁嬮梺鍛婃⒐濡啫顫忔繝姘＜婵炲棙鍨垫俊浠嬫煟鎼达絿鎳楅柛鎰亾缂嶅酣鎮峰鍛暭閻㈩垱甯炴竟鏇犳崉閵娿垹浜鹃悷娆忓缁€鈧┑鐐额嚋缁犳挸顕ｉ崘宸叆闁割偅绻勯鎰攽閻戝洨绉甸柛鎾寸懄娣囧﹥绂掔€ｎ偆鍘介梺瑙勫礃濞夋盯寮稿☉娆樻闁绘劕顕晶顒佺箾閻撳海绠荤€规洘绮忛ˇ鎾煥濞戞艾鏋涙慨濠勫劋鐎电厧鈻庨幋鐘橈綁姊洪崨濠勬噧闁哥喐娼欓锝囨嫚濞村顫嶅┑鐐叉閸旀洟宕濋崨瀛樷拺闂傚牊渚楅悞楣冩煕婵犲啰澧电€规洘婢橀～婵嬵敄閳哄倹顥堥柟顔规櫊濡啫鈽夊Δ鍐╁礋缂傚倸鍊烽懗鍓佸垝椤栨粍鏆滈柨鐔哄Т閺勩儵鏌嶈閸撴岸濡甸崟顖氱闁规惌鍨版慨娑氱磽娴ｅ壊妲洪柡浣割煼瀵鈽夐姀鈥充汗閻庤娲栧ú銈夊煕瀹€鍕拺閻犲洠鈧櫕鐏堝┑鐐点€嬬换婵嬪Υ娴ｅ壊娼╅悹楦挎閸旓箑顪冮妶鍡楃瑨閻庢凹鍓熼幏鎴︽偄閸濄儳顔曢梺鐟扮摠閻熴儵鎮橀埡鍛埞妞ゆ牗鍑瑰〒濠氭煏閸繃顥為柍閿嬪浮閺屾稑螣閻樺弶绁紓宥嗙墬閵囧嫯绠涢幘璺侯杸闂佹娊鏀遍崹鍧楀蓟閻旂厧绠氶柡澶婃櫇閹剧粯鐓涘〒姘ｅ亾濞存粌鐖煎璇测槈閵忕姈鈺呮煏婢舵稓鐣卞ù鐘虫尦閹鈻撻崹顔界亪濡炪値鍘鹃崗姗€鐛崘顔碱潊闁靛牆妫欓崕顏堟⒑闂堚晛鐦滈柛娆忕箳濡叉劙宕ｆ径宀€鐦堢紒鍓у钃辨い顐躬閺屾盯濡搁敃鈧埢鏇犫偓瑙勬礃濞茬喐淇婇崼鏇炵倞闁靛鍎宠ぐ鎾⒒娴ｈ櫣甯涢柛鏃€顨婂畷鏇㈠Χ婢跺﹦鍘遍梺鐟邦嚟婵澹曢挊澹濆綊鏁愰崼顐㈡異闂佺粯甯婄划娆撳蓟瀹ュ鏁嶆繛鎴炵懅椤︻厾绱撴担浠嬪摵閻㈩垽绻濋妴浣糕枎閹惧磭顦ч梺绋跨箳閸樠囨⒒椤栨稓绡€缁剧増菤閸嬫捇宕橀懠顒勭崜闂備礁鎲″褰掓偡閳哄懏鍋樻い鏇楀亾妤犵偞甯￠獮濠囨惞椤愶綆妫冮梺绯曟杹閸嬫挸顪冮妶鍡楃瑨閻庢凹鍙冨畷鏇炍旀担椋庣畾闂侀潧鐗嗙€氼參藝妞嬪海纾奸悹鍥у级椤ョ偤鏌曢崶褍顏€殿喕绮欐俊姝岊檨闁哄棴绻濆铏规嫚閳ュ磭浠╅梺缁橆殔缁绘帒危閹版澘绠抽柟鎯у閹虫繈姊洪幖鐐插妧闁告洦鍘肩紞鍡涙⒒閸屾瑦绁版い鏇熺墵瀹曟澘螖閸涱偀鍋撻崘顔奸唶闁靛鍎抽悿鍛存⒑閸︻叀妾搁柛鐘崇墱缁牏鈧綆鍋佹禍婊堟煙閻戞ê鐏ュù婊呭仦娣囧﹪鎳犻鈧。鑲╃磼缂佹绠橀柛鐘诧攻瀵板嫬鐣濋埀顒勬晬閻斿吋鈷戠紒瀣儥閸庢劖銇勯鐐村枠鐎规洘宀搁獮鎺楀箣閺冣偓閻庡姊虹憴鍕婵炲绋撶划濠囨晝閸屾稈鎷洪梺鍛婄箓鐎氼噣鍩㈡径鎰厱婵☆垱浜介崑銏☆殽閻愭潙鐏撮柟铏矒閹瑩鏌呭☉姘辨晨闂傚倷娴囬～澶婄暦濡　鏋栨繛鎴欏灩閸戠娀骞栧ǎ顒€濡介柣鎾跺枑缁绘繈妫冨☉娆忔閻庤鎸稿Λ娆撳箞閵婏妇绡€闁告劏鏂傛禒銏ゆ倵濞堝灝娅橀柛鎾跺枑娣囧﹪鎮滈懞銉︽珕闂佷紮绲介懟顖滃緤娴犲鈷掗柛灞剧懅椤︼箓鏌熼懞銉х煉鐎规洘濞婃俊鐑藉煛娴ｅ摜鈧參鏌ｉ悩鐑樸€冮悹鈧敃鍌氬惞闁哄洢鍨洪崐鐢告煕閿旇骞栭崯鎼佹⒑濮瑰洤鈧劙宕戦幘缁樷拻濞达絽鎲＄拹锟犳煣韫囨捇鍙勭€规洖缍婇弻鍡楊吋閸涱噮妫熼梻渚€鈧偛鑻晶瀛樻叏婵犲嫮甯涢柟宄版嚇閹煎綊鎮烽幍顕呭仹闂傚倷绀侀幉鈥愁潖閻熸噴娲冀椤掑倷鑸繝鐢靛Х閺佸憡鎱ㄩ弶鎳ㄦ椽濡舵径濠呅曢悷婊呭鐢鎮￠悢鍏肩厸闁稿本绻冪涵鑸电箾閸儰鎲鹃柡宀嬬節閸┾偓妞ゆ帒瀚崵宥夋煏婢舵稓瀵肩紒銊ヮ煼濮婃椽宕崟顓夌娀鏌涢弬璺ㄧ劯鐎规洜鏁婚、妤呭礋椤掑倸骞堥梻渚€娼ч悧鍡椕洪妶澶婂嚑闁哄啫鐗婇悡鍐喐濠婂牆绀堟繛鍡樻尰閸婅埖鎱ㄥ鍡楀⒒闁绘柨妫欓幈銊ヮ渻鐠囪弓澹曢梻浣芥〃缁€渚€宕幘顔衡偓渚€寮崼婵堫槹濡炪倖鎸鹃崰鎰邦敊閺囩姷纾介柛灞剧懅椤︼附銇勯幋婵堝ⅵ妞ゃ垺宀搁獮搴ㄦ寠婢跺瞼娼夐梻渚€鈧偛鑻晶瀛橆殽閻愯尙绠荤€规洏鍔庨埀顒佺⊕鑿ら柟宄扮秺濮婇缚銇愰幒鎴滃枈闂佸憡鎸婚悷銉暰?00闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕偓浠嬫晸閵夆晛纾婚柕蹇嬪€栭悡鏇㈡煟閹邦垰鐨洪柛鈺嬬稻閹便劍绻濋崘鈹夸虎濠碘槅鍋勯崯顐﹀煡婢跺缍囬柕濞垮灪閻忎線姊婚崒娆戭槮闁硅姤绮嶉幈銊╂偨閹肩偐鍋撻崘鈺冪瘈闁稿被鍊曞▓?+ 婵犵數濮烽弫鍛婃叏閻戣棄鏋侀柛娑橈攻閸欏繘鏌ｉ幋锝嗩棄闁哄绶氶弻娑樷槈濮楀牊鏁鹃梺鍛婄懃缁绘﹢寮婚敐澶婎潊闁绘ê妯婂Λ宀勬⒑鏉炴壆顦﹂柨鏇ㄤ邯瀵鍨鹃幇浣告倯闁硅偐琛ラ埀顒€纾鎰版⒒閸屾艾鈧悂宕戦崱娑樺瀭闂侇剙绉存闂佸憡娲﹂崹浼村礃閳ь剟姊洪棃娴ゆ盯宕ㄩ姘瑢缂傚倸鍊搁崐宄懊归崶鈺冪濞村吋娼欑壕瑙勭節闂堟侗鍎忛柦鍐枛閺屻劌鈹戦崱鈺傂ч梺鍝勬噺閻擄繝寮诲☉妯锋闁告鍋為悘宥夋⒑閸︻厼鍘村ù婊冪埣楠炲啫螖閸愨晛鏋傞梺鍛婃处閸撴盯藝閵娾晜鈷戠紓浣股戦幆鍫㈢磼缂佹绠為柣娑卞櫍瀹曟﹢濡告惔銏☆棃鐎规洏鍔戦、娆撴嚍閵壯冪闂傚倷鑳堕、濠囧磻閹邦喗鍋橀柕澶嗘櫅缁€鍫熺節闂堟侗鍎愰柛濠傚閳ь剙绠嶉崕閬嵥囨导鏉戠厱闁瑰濮风壕钘壝归敐鍫濅簵闁瑰濮抽悞濠冦亜閹惧崬鐏柣鎾存礀閳规垿鎮╅幓鎺嗗亾閸︻厽瀚婚柨鐔哄У閻撴瑦顨ラ悙鑼虎闁诲繆鏅犻弻宥囨喆閸曨偆浼岄悗瑙勬礀閻栧ジ宕洪敓鐘茬妞ゅ繐鎷嬪鎾绘⒒閸屾艾鈧兘鎳楅崼鏇椻偓锕傚醇閵夈儱鐝樺銈嗗笒閸婃悂宕瑰┑鍫氬亾閸忓浜鹃梺鍛婃磵閺備線宕戦幘璇茬＜闁绘劕鐡ㄩ崕顏堟⒑闂堚晛鐦滈柛姗€绠栭弫宥咁煥閸涱垳锛濋梺绋挎湰閻熲晛顬婇悜鑺ョ厱闁靛鍎崇粔娲煟濞戝崬娅嶆鐐叉喘閹囧醇閵忕姴绠為梻浣筋嚙閸戠晫绱為崱娑樼；闁告侗鍘鹃弳锕傛倶閻愰潧浜鹃柛娆忕箰閳规垿鎮╅幓鎺濅紑闁汇埄鍨辩粙鎾舵閹烘梻纾兼俊顖濆亹閻ｆ椽姊虹拠鈥崇仭婵☆偄鍟村畷瑙勩偅閸愨晛娈ゅ銈嗗笒閸婂綊寮查妸锔剧瘈闁汇垽娼у暩闂佽桨鐒﹂幃鍌氱暦閹达附鍋愭繛鑼帛閺咁亜鈹戦悩璇у伐闁硅櫕鍔栫粙澶婎吋婢跺鍘甸梺璇″瀻閸滃啰绀婇梻浣告惈濡挳姊介崟顓熷床婵炴垯鍨归獮銏′繆椤栨繃顏犳い蹇曠帛缁绘稓鈧稒顭囬惌濠勭磽瀹ュ拑韬€殿喖顭烽幃銏ゅ礂閻撳簶鍋撶紒妯圭箚妞ゆ牗绻冮鐘裁归悡搴㈠枠婵﹨娅ｉ崠鏍即閻斿摜褰囬梻浣侯焾椤戝啴宕愰弴顫稏闊洦鎷嬪ú顏嶆晜闁告洦鍓欑粊鑸典繆閻愵亜鈧牠骞愰幘顔肩婵犻潧顑愰弫浣逛繆閵堝懏鍣洪柍閿嬪灴閺屾稑鈹戦崟顐㈠闁哄秮鈧剚娓婚柕鍫濆暙婵″ジ鏌ㄩ弴銊ら偗闁绘侗鍣ｅ畷姗€顢欓懖鈺€绱滈梻浣告惈閸婂爼宕曢弻銉稏鐎广儱顦伴埛鎴︽⒒閸喓娲撮柣娑欑矌缁辨帗娼忛妸锔绢槹閻庤娲忛崹浠嬪蓟閸℃鍚嬮柛鈩冪懃楠炴姊绘繝搴′簻婵炶绠戠叅闁哄秲鍔庨々閿嬬節婵犲倸鏋ら柣鏂挎閹叉瓕绠涘☉妯兼煣濡炪倖鍔戦崐鏇㈠垂濠靛棌鏀介柣妯虹枃婢规﹢宕鐐粹拺闁圭瀛╅悡銉ヮ熆瑜岀划娆撱€佸▎鎾冲嵆闁靛繆妾ч幏铏圭磽娴ｅ壊鍎撴繛澶嬫礈缁骞庨懞銉у幈濠碘槅鍨虫慨瀵糕偓姘煎弮瀹曪綀绠涢幙鍐數闂佸吋鎮傚褎鎱ㄩ崼銉︾厽闁规崘娉涢弸娑㈡煛鐏炵偓绀冪€垫澘瀚灒闁绘垶顭囨禍鐐烘⒒閸屾瑧璐伴柛娆忛叄瀹曞綊鏌嗗鍛傦箓鏌涢弴銊ョ仩妞ゎ偄鎳橀弻宥夋寠婢跺娈岄梺閫炲苯澧繛纭风節瀵鎮㈢喊杈ㄦ櫓闂佷紮绲介張顒勫闯瑜嶉—鍐Χ閸℃鍙嗙紓浣虹帛閿曘垽濡存担绯曟瀻闁圭偓娼欏▓鐔兼⒑闂堟侗妯堥柛鐘崇墵瀵煡濡烽埡鍌楁嫽?0闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕偓浠嬫晸閵夆晛纾婚柕蹇嬪€栭悡鏇㈡煟閹邦垰鐨洪柛鈺嬬稻閹便劍绻濋崘鈹夸虎濠碘槅鍋勯崯顐﹀煡婢跺缍囬柕濞垮灪閻忎線姊婚崒娆戭槮闁硅姤绮嶉幈銊╂偨閹肩偐鍋撻崘鈺冪瘈闁稿被鍊曞▓?
            int totalMethods=0;
            for(Assign a: assigns){
                totalMethods+=a.c2s.size()+a.s2c.size();
            }
            StringBuilder sb=new StringBuilder(200+totalMethods*50);
            sb.append("package ").append(pkg).append(";\n\n");
            sb.append("public final class ProtoIds {\n");
            int max=0;
            for(Assign a: assigns){
                int id=a.c2sStart;
                for(Method m: a.c2s){
                    sb.append("    public static final int ").append(a.baseCamel.toUpperCase()).append("_").append(m.name.toUpperCase()).append(" = ").append(id).append(";\n");
                    max=Math.max(max,id);
                    id+=2;
                }
                id=a.s2cStart;
                for(Method m: a.s2c){
                    sb.append("    public static final int ").append(a.baseCamel.toUpperCase()).append("_").append(m.name.toUpperCase()).append(" = ").append(id).append(";\n");
                    max=Math.max(max,id);
                    id+=2;
                }
            }
            sb.append("    public static final int MAX_ID = ").append(max).append(";\n");
            sb.append("}\n");
            return sb.toString();
        }
        static String generateDispatcher(String protoPkg, String boPkg, List<Assign> assigns){
            // 濠电姷鏁告慨鐑藉极閸涘﹥鍙忛柣鎴ｆ閺嬩線鏌涘☉姗堟敾闁告瑥绻橀弻锝夊箣濠垫劖缍楅梺閫炲苯澧柛濠傛健楠炴劖绻濋崘顏嗗骄闂佸啿鎼鍥╃矓椤旈敮鍋撶憴鍕８闁告梹鍨甸锝夊醇閺囩偟顓洪梺缁樼懃閹虫劙鐛姀銈嗏拻闁稿本鐟чˇ锕傛煙濞村澧茬紒妤冨枎铻栭柛娑卞幘閻撴垿鏌熼崗鑲╂殬闁告柨绉瑰畷鎴﹀礋椤栨稓鍘遍梺鏂ユ櫅閸橀箖鎳栭埡鍌氬簥闂佺硶鍓濊彠濞存粍绮撻弻鈥愁吋閸愩劌顬夐梺姹囧妽閸ㄥ爼骞堥妸鈺傛櫜闁搞儜鍌涱潟闂備礁鎼張顒傜矙閹捐鐒垫い鎺戯功缁夌敻鏌涚€ｎ亝鍣藉ù婊勬倐椤㈡﹢鎮㈢紙鐘电泿婵＄偑鍊栭崝褏寰婄捄銊т笉闁绘劗鍎ら悡娆愩亜閺冨倹鍤€濠⒀勭叀閺岀喖顢涘☉娆樻闂佺硶鏅粻鎾诲春閳ь剚銇勯幒鎴濐仼缂佺媭鍨遍妵鍕箛閸洘顎嶉梺缁樻尵閸犳牠鐛弽顬ュ酣顢楅埀顒勫焵椤戞儳鈧洟鈥﹂崶顒€绠涙い鎾跺Х椤旀洟姊洪崨濠勬噧妞わ箒浜划濠氭倷閻戞鍙嗗┑鐘绘涧閻楀棙绂掗敂閿亾閸偅绶查悗姘嵆閻涱噣宕堕澶嬫櫌闂佺鏈划宥呅掓惔銊︹拻闁稿本鐟чˇ锕傛煙绾板崬浜扮€规洦鍨堕、鏇㈡晜閽樺缃曢梻浣虹《閸撴繈鏁嬮梺鍛婃⒐濡啫顫忔繝姘＜婵炲棙鍨垫俊浠嬫煟鎼达絿鎳楅柛鎰亾缂嶅酣鎮峰鍛暭閻㈩垱甯炴竟鏇犳崉閵娿垹浜鹃悷娆忓缁€鈧┑鐐额嚋缁犳挸顕ｉ崘宸叆闁割偅绻勯鎰攽閻戝洨绉甸柛鎾寸懄娣囧﹥绂掔€ｎ偆鍘介梺瑙勫礃濞夋盯寮稿☉娆樻闁绘劕顕晶顒佺箾閻撳海绠荤€规洘绮忛ˇ鎾煥濞戞艾鏋涙慨濠勫劋鐎电厧鈻庨幋鐘橈綁姊洪崨濠勬噧闁哥喐娼欓锝囨嫚濞村顫嶅┑鐐叉閸旀洟宕濋崨瀛樷拺闂傚牊渚楅悞楣冩煕婵犲啰澧电€规洘婢橀～婵嬵敄閳哄倹顥堥柟顔规櫊濡啫鈽夊Δ鍐╁礋缂傚倸鍊烽懗鍓佸垝椤栨粍鏆滈柨鐔哄Т閺勩儵鏌嶈閸撴岸濡甸崟顖氱闁规惌鍨版慨娑氱磽娴ｅ壊妲洪柡浣割煼瀵鈽夐姀鈥充汗閻庤娲栧ú銈夊煕瀹€鍕拺閻犲洠鈧櫕鐏堝┑鐐点€嬬换婵嬪Υ娴ｅ壊娼╅悹楦挎閸旓箑顪冮妶鍡楃瑨閻庢凹鍓熼幏鎴︽偄閸濄儳顔曢梺鐟扮摠閻熴儵鎮橀埡鍛埞妞ゆ牗鍑瑰〒濠氭煏閸繃顥為柍閿嬪浮閺屾稑螣閻樺弶绁紓宥嗙墬閵囧嫯绠涢幘璺侯杸闂佹娊鏀遍崹鍧楀蓟閻旂厧绠氶柡澶婃櫇閹剧粯鐓涘〒姘ｅ亾濞存粌鐖煎璇测槈閵忕姈鈺呮煏婢舵稓鐣卞ù鐘虫尦閹鈻撻崹顔界亪濡炪値鍘鹃崗姗€鐛崘顔碱潊闁靛牆妫欓崕顏堟⒑闂堚晛鐦滈柛娆忕箳濡叉劙宕ｆ径宀€鐦堢紒鍓у钃辨い顐躬閺屾盯濡搁敃鈧埢鏇犫偓瑙勬礃濞茬喐淇婇崼鏇炵倞闁靛鍎宠ぐ鎾⒒娴ｈ櫣甯涢柛鏃€顨婂畷鏇㈠Χ婢跺﹦鍘遍梺鐟邦嚟婵澹曢挊澹濆綊鏁愰崼顐㈡異闂佺粯甯婄划娆撳蓟瀹ュ鏁嶆繛鎴炵懅椤︻厾绱撴担浠嬪摵閻㈩垽绻濋妴浣糕枎閹惧磭顦ч梺绋跨箳閸樠囨⒒椤栨稓绡€缁剧増菤閸嬫捇宕橀懠顒勭崜闂備礁鎲″褰掓偡閳哄懏鍋樻い鏇楀亾妤犵偞甯￠獮濠囨惞椤愶綆妫冮梺绯曟杹閸嬫挸顪冮妶鍡楃瑨閻庢凹鍙冨畷鏇炍旀担椋庣畾闂侀潧鐗嗙€氼參藝妞嬪海纾奸悹鍥у级椤ョ偤鏌曢崶褍顏€殿喕绮欐俊姝岊檨闁哄棴绻濆铏规嫚閳ュ磭浠╅梺缁橆殔缁绘帒危閹版澘绠抽柟鎯у閹虫繈姊洪幖鐐插妧闁告洦鍘肩紞鍡涙⒒閸屾瑦绁版い鏇熺墵瀹曟澘螖閸涱偀鍋撻崘顔奸唶闁靛鍎抽悿鍛存⒑閸︻叀妾搁柛鐘崇墱缁牏鈧綆鍋佹禍婊堟煙閻戞ê鐏ュù婊呭仦娣囧﹪鎳犻鈧。鑲╃磼缂佹绠橀柛鐘诧攻瀵板嫬鐣濋埀顒勬晬閻斿吋鈷戠紒瀣儥閸庢劖銇勯鐐村枠鐎规洘宀搁獮鎺楀箣閺冣偓閻庡姊虹憴鍕婵炲绋撶划濠囨晝閸屾稈鎷洪梺鍛婄箓鐎氼噣鍩㈡径鎰厱婵☆垱浜介崑銏☆殽閻愭潙鐏撮柟铏矒閹瑩鏌呭☉姘辨晨闂傚倷娴囬～澶婄暦濡　鏋栨繛鎴欏灩閸戠娀骞栧ǎ顒€濡介柣鎾跺枑缁绘繈妫冨☉娆忔閻庤鎸稿Λ娆撳箞閵婏妇绡€闁告劏鏂傛禒銏ゆ倵濞堝灝娅橀柛鎾跺枑娣囧﹪鎮滈懞銉︽珕闂佷紮绲介懟顖滃緤娴犲鈷掗柛灞剧懅椤︼箓鏌熼懞銉х煉鐎规洘濞婃俊鐑藉煛娴ｅ摜鈧參鏌ｉ悩鐑樸€冮悹鈧敃鍌氬惞闁哄洢鍨洪崐鐢告煕閿旇骞栭崯鎼佹⒑濮瑰洤鈧劙宕戦幘缁樷拻濞达絽鎲＄拹锟犳煣韫囨捇鍙勭€规洖缍婇弻鍡楊吋閸涱噮妫熼梻渚€鈧偛鑻晶瀛樻叏婵犲嫮甯涢柟宄版嚇閹煎綊鎮烽幍顕呭仹闂傚倷绀侀幉鈥愁潖閻熸噴娲冀椤掑倷鑸繝鐢靛Х閺佸憡鎱ㄩ弶鎳ㄦ椽濡舵径濠呅曢悷婊呭鐢鎮￠悢鍏肩厸闁稿本绻冪涵鑸电箾閸儰鎲鹃柡宀嬬節閸┾偓妞ゆ帒瀚崵宥夋煏婢舵稓瀵肩紒銊ヮ煼濮婃椽宕崟顓夌娀鏌涢弬璺ㄧ劯鐎规洜鏁婚、妤呭礋椤掑倸骞堥梻渚€娼ч悧鍡椕洪妶澶婂嚑闁哄啫鐗婇悡鍐喐濠婂牆绀堟繛鍡樻尰閸婅埖鎱ㄥ鍡楀⒒闁绘柨妫欓幈銊ヮ渻鐠囪弓澹曢梻浣芥〃缁€渚€宕幘顔衡偓渚€寮崼婵堫槹濡炪倖鎸鹃崰鎰邦敊閺囩姷纾介柛灞剧懅椤︼附銇勯幋婵堝ⅵ妞ゃ垺宀搁獮搴ㄦ寠婢跺瞼娼夐梻渚€鈧偛鑻晶瀛橆殽閻愯尙绠荤€规洏鍔庨埀顒佺⊕鑿ら柟宄扮秺濮婇缚銇愰幒鎴滃枈闂佸憡鎸婚悷銉暰?500闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕偓浠嬫晸閵夆晛纾婚柕蹇嬪€栭悡鏇㈡煟閹邦垰鐨洪柛鈺嬬稻閹便劍绻濋崘鈹夸虎濠碘槅鍋勯崯顐﹀煡婢跺缍囬柕濞垮灪閻忎線姊婚崒娆戭槮闁硅姤绮嶉幈銊╂偨閹肩偐鍋撻崘鈺冪瘈闁稿被鍊曞▓?+ 婵犵數濮烽弫鍛婃叏閻戣棄鏋侀柛娑橈攻閸欏繘鏌ｉ幋锝嗩棄闁哄绶氶弻娑樷槈濮楀牊鏁鹃梺鍛婄懃缁绘﹢寮婚敐澶婎潊闁绘ê妯婂Λ宀勬⒑鏉炴壆顦﹂柨鏇ㄤ邯瀵鍨鹃幇浣告倯闁硅偐琛ラ埀顒€纾鎰版⒒閸屾艾鈧悂宕戦崱娑樺瀭闂侇剙绉存闂佸憡娲﹂崹浼村礃閳ь剟姊洪棃娴ゆ盯宕ㄩ姘瑢缂傚倸鍊搁崐宄懊归崶鈺冪濞村吋娼欑壕瑙勭節闂堟侗鍎忛柦鍐枛閺屻劌鈹戦崱鈺傂ч梺鍝勬噺閻擄繝寮诲☉妯锋闁告鍋為悘宥夋⒑閸︻厼鍘村ù婊冪埣楠炲啫螖閸愨晛鏋傞梺鍛婃处閸撴盯藝閵娾晜鈷戠紓浣股戦幆鍫㈢磼缂佹绠為柣娑卞櫍瀹曟﹢濡告惔銏☆棃鐎规洏鍔戦、娆撴嚍閵壯冪闂傚倷鑳堕、濠囧磻閹邦喗鍋橀柕澶嗘櫅缁€鍫熺節闂堟侗鍎愰柛濠傚閳ь剙绠嶉崕閬嵥囨导鏉戠厱闁瑰濮风壕钘壝归敐鍫濅簵闁瑰濮抽悞濠冦亜閹惧崬鐏柣鎾存礀閳规垿鎮╅幓鎺嗗亾閸︻厽瀚婚柨鐔哄У閻撴瑦顨ラ悙鑼虎闁诲繆鏅犻弻宥囨喆閸曨偆浼岄悗瑙勬礀閻栧ジ宕洪敓鐘茬妞ゅ繐鎷嬪鎾绘⒒閸屾艾鈧兘鎳楅崼鏇椻偓锕傚醇閵夈儱鐝樺銈嗗笒閸婃悂宕瑰┑鍫氬亾閸忓浜鹃梺鍛婃磵閺備線宕戦幘璇茬＜闁绘劕鐡ㄩ崕顏堟⒑闂堚晛鐦滈柛姗€绠栭弫宥咁煥閸涱垳锛濋梺绋挎湰閻熲晛顬婇悜鑺ョ厱闁靛鍎崇粔娲煟濞戝崬娅嶆鐐叉喘閹囧醇閵忕姴绠為梻浣筋嚙閸戠晫绱為崱娑樼；闁告侗鍘鹃弳锕傛倶閻愰潧浜鹃柛娆忕箰閳规垿鎮╅幓鎺濅紑闁汇埄鍨辩粙鎾舵閹烘梻纾兼俊顖濆亹閻ｆ椽姊虹拠鈥崇仭婵☆偄鍟村畷瑙勩偅閸愨晛娈ゅ銈嗗笒閸婂綊寮查妸锔剧瘈闁汇垽娼у暩闂佽桨鐒﹂幃鍌氱暦閹达附鍋愭繛鑼帛閺咁亜鈹戦悩璇у伐闁硅櫕鍔栫粙澶婎吋婢跺鍘甸梺璇″瀻閸滃啰绀婇梻浣告惈濡挳姊介崟顓熷床婵炴垯鍨归獮銏′繆椤栨繃顏犳い蹇曠帛缁绘稓鈧稒顭囬惌濠勭磽瀹ュ拑韬€殿喖顭烽幃銏ゅ礂閻撳簶鍋撶紒妯圭箚妞ゆ牗绻冮鐘裁归悡搴㈠枠婵﹨娅ｉ崠鏍即閻斿摜褰囬梻浣侯焾椤戝啴宕愰弴顫稏闊洦鎷嬪ú顏嶆晜闁告洦鍓欑粊鑸典繆閻愵亜鈧牠骞愰幘顔肩婵犻潧顑愰弫浣逛繆閵堝懏鍣洪柍閿嬪灴閺屾稑鈹戦崟顐㈠闁哄秮鈧剚娓婚柕鍫濆暙婵″ジ鏌ㄩ弴銊ら偗闁绘侗鍣ｅ畷姗€顢欓懖鈺€绱滈梻浣告惈閸婂爼宕曢弻銉稏鐎广儱顦伴埛鎴︽⒒閸喓娲撮柣娑欑矌缁辨帗娼忛妸锔绢槹閻庤娲忛崹浠嬪蓟閸℃鍚嬮柛鈩冪懃楠炴姊绘繝搴′簻婵炶绠戠叅闁哄秲鍔庨々閿嬬節婵犲倸鏋ら柣鏂挎閹叉瓕绠涘☉妯兼煣濡炪倖鍔戦崐鏇㈠垂濠靛棌鏀介柣妯虹枃婢规﹢宕鐐粹拺闁圭瀛╅悡銉ヮ熆瑜岀划娆撱€佸▎鎾冲嵆闁靛繆妾ч幏铏圭磽娴ｅ壊鍎撴繛澶嬫礈缁骞庨懞銉у幈濠碘槅鍨虫慨瀵糕偓姘煎弮瀹曪綀绠涢幙鍐數闂佸吋鎮傚褎鎱ㄩ崼銉︾厽闁规崘娉涢弸娑㈡煛鐏炵偓绀冪€垫澘瀚灒闁绘垶顭囨禍鐐烘⒒閸屾瑧璐伴柛娆忛叄瀹曞綊鏌嗗鍛傦箓鏌涢弴銊ョ仩妞ゎ偄鎳橀弻宥夋寠婢跺娈岄梺閫炲苯澧繛纭风節瀵鎮㈢喊杈ㄦ櫓闂佷紮绲介張顒勫闯瑜嶉—鍐Χ閸℃鍙嗙紓浣虹帛閿曘垽濡存担绯曟瀻闁圭偓娼欏▓鐔兼⒑闂堟侗妯堥柛鐘崇墵瀵煡濡烽埡鍌楁嫽?00闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不婵犳碍鐓曢柍瑙勫劤娴滅偓淇婇悙顏勨偓鏍暜婵犲洦鍤勯柛顐ｆ礀閻撴繈鏌熼崜褏甯涢柣鎾寸洴閺屾稑鈽夐崡鐐寸亾缂備胶濮甸敃銏ゅ蓟濞戙垹绠抽柟鎯х－閻熴劑姊虹€圭媭鍤欓梺甯秮閻涱喖螣閾忚娈鹃梺鎼炲劥濞夋盯寮挊澶嗘斀闁绘ɑ顔栭弳婊呯磼鏉堛劍绀嬬€规洘鍨甸埥澶愬閳ュ啿澹勯梻浣虹帛閸ㄧ厧螞閸曨厼顥氬┑鐘崇閻撴瑩鏌熺憴鍕Е闁搞倖鐟х槐鎺楀焵椤掑嫬绀冮柍鐟般仒缁ㄥ姊洪崫鍕偓浠嬫晸閵夆晛纾婚柕蹇嬪€栭悡鏇㈡煟閹邦垰鐨洪柛鈺嬬稻閹便劍绻濋崘鈹夸虎濠碘槅鍋勯崯顐﹀煡婢跺缍囬柕濞垮灪閻忎線姊婚崒娆戭槮闁硅姤绮嶉幈銊╂偨閹肩偐鍋撻崘鈺冪瘈闁稿被鍊曞▓?
            int totalMethods=0;
            for(Assign a: assigns){
                totalMethods+=a.c2s.size()+a.s2c.size();
            }
            StringBuilder sb=new StringBuilder(1500+totalMethods*100);
            sb.append("package ").append(boPkg).append(";\n\n");
            sb.append("import io.netty.channel.Channel;\n");
            sb.append("import java.util.*;\n");
            sb.append("import ").append(JavaRuntimeSupport.bytesPackage(protoPkg)).append(".*;\n");
            sb.append("import ").append(JavaRuntimeSupport.protoPackage(protoPkg)).append(".IProtoDispatch;\n");
            sb.append("import ").append(protoPkg).append(".ProtoIds;\n");
            sb.append("import ").append(protoPkg).append(".*;\n");
            sb.append("import ").append(protoPkg).append(".runtime.serialize.*;\n\n");
            sb.append("public class ProtoDispatchManager implements IProtoDispatch {\n");
            sb.append("    @FunctionalInterface interface H { void h(Channel ch, io.netty.buffer.ByteBuf buf); }\n");
            sb.append("    private final IntObjectHashMap<H> table;\n");
            for(Assign a: assigns){
                sb.append("    private ").append("I").append(a.baseCamel).append("BO ").append(lowerFirst(a.baseCamel)).append("BO;\n");
            }
            sb.append("    public ProtoDispatchManager(){ this.table=new IntObjectHashMap<>(").append(Math.max(4, totalMethods)).append("); }\n");
            for(Assign a: assigns){
                sb.append("    public void register").append(a.baseCamel).append("(").append("I").append(a.baseCamel).append("BO bo){ this.").append(lowerFirst(a.baseCamel)).append("BO=bo;\n");
                int id=a.c2sStart;
                for(Method m: a.c2s){
                    List<Field> ps=m.params;
                    List<Field> presenceFields=presenceFields(ps);
                    sb.append("        table.putInt(").append(id).append(", (ch,buf)->{\n");
                    appendJavaPresenceReadPrelude(sb, presenceFields.size(), "buf", "            ", "ByteIO");
                    int presenceIndex=0;
                    for(int i=0;i<ps.size();i++){
                        Field f=ps.get(i);
                        sb.append("            ").append(mapType(f.type)).append(" p").append(i).append(" = ");
                        if(isPresenceTrackedType(f.type)){
                            String presentExpr=javaPresenceExpr("__presence", presenceIndex++, presenceFields.size());
                            if(isOptionalType(f.type)){
                                String inner=genericBody(f.type).trim();
                                sb.append(presentExpr)
                                        .append(" ? Optional.ofNullable(").append(readValue("buf", inner)).append(") : Optional.empty()");
                            }else{
                                sb.append(presentExpr)
                                        .append(" ? ").append(readValue("buf", f.type)).append(" : ").append(javaDefaultValueExpr(f.type));
                            }
                        }else{
                            sb.append(readValue("buf", f.type));
                        }
                        sb.append(";\n");
                    }
                    sb.append("            ").append(lowerFirst(a.baseCamel)).append("BO.").append(m.name).append("(ch");
                    for(int i=0;i<ps.size();i++) sb.append(", p").append(i);
                    sb.append(");\n");
                    sb.append("        });\n");
                    id+=2;
                }
                sb.append("    }\n");
            }
            sb.append("    @Override public void dispatch(Channel ch, int id, io.netty.buffer.ByteBuf payload){ H h=table.getInt(id); if(h==null) return; h.h(ch,payload); }\n");
            sb.append("}\n");
            return sb.toString();
        }
        static String lowerFirst(String s){ return Character.toLowerCase(s.charAt(0))+s.substring(1); }
        static String childVar(String base, String suffix){
            String clean=sanitizeIdentifier(base);
            return clean + Character.toUpperCase(suffix.charAt(0)) + suffix.substring(1);
        }
        static String sanitizeIdentifier(String value){
            if(value==null || value.isEmpty()) return "v";
            StringBuilder sb=new StringBuilder();
            boolean upperNext=false;
            for(int i=0;i<value.length();i++){
                char c=value.charAt(i);
                if(Character.isLetterOrDigit(c)){
                    if(sb.length()==0){
                        if(Character.isDigit(c)) sb.append('v');
                        sb.append(Character.toLowerCase(c));
                    }else if(upperNext){
                        sb.append(Character.toUpperCase(c));
                        upperNext=false;
                    }else{
                        sb.append(c);
                    }
                }else if(sb.length()>0){
                    upperNext=true;
                }
            }
            if(sb.length()==0) return "v";
            return sb.toString();
        }
        static List<Field> optionalFields(List<Field> fields){
            List<Field> optionalFields=new ArrayList<>();
            for(Field field: fields){
                if(isOptionalType(field.type)) optionalFields.add(field);
            }
            return optionalFields;
        }
        static List<Field> presenceFields(List<Field> fields){
            List<Field> tracked=new ArrayList<>();
            for(Field field: fields){
                if(isPresenceTrackedType(field.type)) tracked.add(field);
            }
            return tracked;
        }
        static boolean isPresenceTrackedType(String t){
            return isOptionalType(t)
                    || isPrimitive(t)
                    || t.equals("String")
                    || ENUMS.contains(t)
                    || t.endsWith("[]")
                    || isListLikeType(t)
                    || isSetLikeType(t)
                    || isQueueLikeType(t)
                    || isMapLikeType(t)
                    || t.equals("Integer")
                    || t.equals("Long")
                    || t.equals("Byte")
                    || t.equals("Short")
                    || t.equals("Boolean")
                    || t.equals("Character")
                    || t.equals("Float")
                    || t.equals("Double");
        }
        static String javaHasWireValueExpr(String valueExpr, Field f){
            if(isBorrowedBytesField(f)){
                return valueExpr+"!=null && "+valueExpr+".length()!=0";
            }
            if(isBorrowedStringField(f)){
                return valueExpr+"!=null && "+valueExpr+".byteLength()!=0";
            }
            if(isBorrowedPrimitiveArrayField(f)){
                return valueExpr+"!=null && "+valueExpr+".count()!=0";
            }
            return javaHasWireValueExpr(valueExpr, f.type);
        }
        static String javaHasWireValueExpr(String valueExpr, String t){
            if(isOptionalType(t)) return optionalPresentExpr(valueExpr);
            if(t.equals("int")) return valueExpr+"!=0";
            if(t.equals("Integer")) return valueExpr+"!=null && "+valueExpr+"!=0";
            if(t.equals("long")) return valueExpr+"!=0L";
            if(t.equals("Long")) return valueExpr+"!=null && "+valueExpr+"!=0L";
            if(t.equals("byte")) return valueExpr+"!=(byte)0";
            if(t.equals("Byte")) return valueExpr+"!=null && "+valueExpr+"!=(byte)0";
            if(t.equals("short")) return valueExpr+"!=(short)0";
            if(t.equals("Short")) return valueExpr+"!=null && "+valueExpr+"!=(short)0";
            if(t.equals("boolean")) return valueExpr;
            if(t.equals("Boolean")) return valueExpr+"!=null && "+valueExpr;
            if(t.equals("char")) return valueExpr+"!=(char)0";
            if(t.equals("Character")) return valueExpr+"!=null && "+valueExpr+"!=(char)0";
            if(t.equals("float")) return valueExpr+"!=0F";
            if(t.equals("Float")) return valueExpr+"!=null && "+valueExpr+"!=0F";
            if(t.equals("double")) return valueExpr+"!=0D";
            if(t.equals("Double")) return valueExpr+"!=null && "+valueExpr+"!=0D";
            if(t.equals("String")) return valueExpr+"!=null && !"+valueExpr+".isEmpty()";
            if(ENUMS.contains(t)) return valueExpr+"!=null && "+valueExpr+".ordinal()!=0";
            if(t.endsWith("[]")) return valueExpr+"!=null && "+valueExpr+".length!=0";
            if(isListLikeType(t) || isSetLikeType(t) || isQueueLikeType(t) || isMapLikeType(t)){
                return valueExpr+"!=null && !"+valueExpr+".isEmpty()";
            }
            return "true";
        }
        static String javaDefaultValueExpr(Field f){
            if(isBorrowedBytesField(f)){
                return "BorrowedBytes.empty()";
            }
            if(isBorrowedStringField(f)){
                return "BorrowedString.empty()";
            }
            if(isBorrowedPrimitiveArrayField(f)){
                return borrowedArrayEmptyExpr(f.type);
            }
            if(isPackedIntIntMapField(f)){
                return "ByteIO.borrowIntIntHashMap(0)";
            }
            if(isPackedIntLongMapField(f)){
                return "ByteIO.borrowIntLongHashMap(0)";
            }
            if(isPackedIntKeyObjectMapField(f)){
                return "ByteIO.borrowIntObjectHashMap(0)";
            }
            return javaDefaultValueExpr(f.type);
        }
        static String javaDefaultValueExpr(String t){
            if(isOptionalType(t)) return "Optional.empty()";
            if(t.equals("int") || t.equals("Integer")) return "0";
            if(t.equals("long") || t.equals("Long")) return "0L";
            if(t.equals("byte") || t.equals("Byte")) return "(byte)0";
            if(t.equals("short") || t.equals("Short")) return "(short)0";
            if(t.equals("boolean") || t.equals("Boolean")) return "false";
            if(t.equals("char") || t.equals("Character")) return "(char)0";
            if(t.equals("float") || t.equals("Float")) return "0F";
            if(t.equals("double") || t.equals("Double")) return "0D";
            if(t.equals("String")) return "\"\"";
            if(ENUMS.contains(t)) return t+".fromOrdinal(0)";
            if(t.endsWith("[]")){
                return javaArrayAllocationExpr(t, "0");
            }
            if(isListLikeType(t)){
                String inner=mapType(genericBody(t).trim());
                if("LinkedList".equals(canonicalContainerType(t))){
                    return "new LinkedList<"+inner+">()";
                }
                if(isSpecializedIntListType(t)){
                    return "ByteIO.borrowIntArrayList(0)";
                }
                if(isSpecializedLongListType(t)){
                    return "ByteIO.borrowLongArrayList(0)";
                }
                return "ByteIO.borrowArrayList(0)";
            }
            if(isSetLikeType(t)){
                String inner=mapType(genericBody(t).trim());
                if("LinkedHashSet".equals(canonicalContainerType(t))){
                    return "ByteIO.borrowLinkedHashSet(0)";
                }
                return "ByteIO.borrowHashSet(0)";
            }
            if(isQueueLikeType(t)){
                return "ByteIO.borrowArrayDeque(0)";
            }
            if(isMapLikeType(t)){
                List<String> kv=splitTopLevel(genericBody(t), ',');
                String kt=mapType(kv.get(0).trim());
                String vt=mapType(kv.get(1).trim());
                if("LinkedHashMap".equals(canonicalContainerType(t))){
                    return "ByteIO.borrowLinkedHashMap(0)";
                }
                if(isSpecializedIntIntMapType(t)){
                    return "ByteIO.borrowIntIntHashMap(0)";
                }
                if(isSpecializedIntLongMapType(t)){
                    return "ByteIO.borrowIntLongHashMap(0)";
                }
                if(isSpecializedIntObjectMapType(t)){
                    return "ByteIO.borrowIntObjectHashMap(0)";
                }
                return "ByteIO.borrowHashMap(0)";
            }
            return "null";
        }
        static String javaProjectionDefaultValueExpr(String t){
            if(t.equals("int")) return "0";
            if(t.equals("long")) return "0L";
            if(t.equals("byte")) return "(byte)0";
            if(t.equals("short")) return "(short)0";
            if(t.equals("boolean")) return "false";
            if(t.equals("char")) return "(char)0";
            if(t.equals("float")) return "0F";
            if(t.equals("double")) return "0D";
            return "null";
        }
        static boolean useSinglePresenceWord(int optionalCount){
            return optionalCount<=Long.SIZE;
        }
        static boolean useJavaFullPresenceFastPath(int optionalCount){
            return optionalCount>0 && useSinglePresenceWord(optionalCount);
        }
        static boolean isDominantMaskSuffixEligible(String t){
            return t.equals("String")
                    || isOptionalType(t)
                    || t.endsWith("[]")
                    || isListLikeType(t)
                    || isSetLikeType(t)
                    || isQueueLikeType(t)
                    || isMapLikeType(t);
        }
        static List<Integer> dominantMaskPresentCounts(List<Field> presenceFields){
            int optionalCount=presenceFields.size();
            if(optionalCount<=0 || !useSinglePresenceWord(optionalCount)){
                return java.util.Collections.emptyList();
            }
            ArrayList<Integer> counts=new ArrayList<>();
            counts.add(optionalCount);
            int suffixFamilies=0;
            for(int i=optionalCount-1;i>=0 && suffixFamilies<4;i--){
                if(!isDominantMaskSuffixEligible(presenceFields.get(i).type)){
                    break;
                }
                if(i>0){
                    counts.add(i);
                }
                suffixFamilies++;
            }
            return counts;
        }
        static boolean useJavaDominantMaskFamilies(List<Field> presenceFields, boolean hotMode){
            return hotMode && dominantMaskPresentCounts(presenceFields).size()>1;
        }
        static String javaFullPresenceMaskLiteral(int optionalCount){
            if(optionalCount<=0){
                return "0L";
            }
            if(optionalCount>=Long.SIZE){
                return "-1L";
            }
            long mask=(1L<<optionalCount)-1L;
            return "0x"+Long.toHexString(mask).toUpperCase(java.util.Locale.ROOT)+"L";
        }
        static String javaPresencePrefixMaskLiteral(int presentCount){
            return javaFullPresenceMaskLiteral(presentCount);
        }
        static String javaAllPresentExpr(List<Field> presenceFields, String valuePrefix){
            StringBuilder expr=new StringBuilder();
            for(int i=0;i<presenceFields.size();i++){
                if(i>0){
                    expr.append(" && ");
                }
                Field field=presenceFields.get(i);
                expr.append(javaHasWireValueExpr(valuePrefix+field.name, field));
            }
            return expr.toString();
        }
        static String optionalPresentExpr(String valueExpr){
            return valueExpr+"!=null && "+valueExpr+".isPresent()";
        }
        static void appendJavaPresenceWritePrelude(StringBuilder sb, List<Field> optionalFields, String valuePrefix, String bufVar, String indent){
            appendJavaPresenceWritePrelude(sb, optionalFields, valuePrefix, bufVar, indent, "BufUtil", "__presence");
        }
        static void appendJavaPresenceWritePrelude(StringBuilder sb, List<Field> optionalFields, String valuePrefix, String bufVar, String indent, String ioClass){
            appendJavaPresenceWritePrelude(sb, optionalFields, valuePrefix, bufVar, indent, ioClass, "__presence");
        }
        static void appendJavaPresenceWritePrelude(StringBuilder sb, List<Field> optionalFields, String valuePrefix, String bufVar, String indent, String ioClass, String presenceVar){
            if(optionalFields.isEmpty()) return;
            int optionalCount=optionalFields.size();
            if(useSinglePresenceWord(optionalCount)){
                sb.append(indent).append("long ").append(presenceVar).append("=0L;\n");
                for(int i=0;i<optionalCount;i++){
                    Field field=optionalFields.get(i);
                    String fieldExpr=valuePrefix+field.name;
                    sb.append(indent).append("if(").append(javaHasWireValueExpr(fieldExpr, field)).append(") ").append(presenceVar).append(" |= 1L << ").append(i).append(";\n");
                }
                sb.append(indent).append(ioClass).append(".writePresenceBits(").append(bufVar).append(", ").append(presenceVar).append(", ").append(optionalCount).append(");\n");
                return;
            }
            sb.append(indent).append("long[] ").append(presenceVar).append("=new long[").append((optionalCount+63)>>>6).append("];\n");
            for(int i=0;i<optionalCount;i++){
                Field field=optionalFields.get(i);
                String fieldExpr=valuePrefix+field.name;
                sb.append(indent).append("if(").append(javaHasWireValueExpr(fieldExpr, field)).append(") ").append(presenceVar).append("[")
                        .append(i>>>6).append("] |= 1L << ").append(i&63).append(";\n");
            }
            sb.append(indent).append(ioClass).append(".writePresenceBits(").append(bufVar).append(", ").append(presenceVar).append(", ").append(optionalCount).append(");\n");
        }
        static void appendJavaPresenceReadPrelude(StringBuilder sb, int optionalCount, String bufVar, String indent){
            appendJavaPresenceReadPrelude(sb, optionalCount, bufVar, indent, "BufUtil", "__presence");
        }
        static void appendJavaPresenceReadPrelude(StringBuilder sb, int optionalCount, String bufVar, String indent, String ioClass){
            appendJavaPresenceReadPrelude(sb, optionalCount, bufVar, indent, ioClass, "__presence");
        }
        static void appendJavaPresenceReadPrelude(StringBuilder sb, int optionalCount, String bufVar, String indent, String ioClass, String presenceVar){
            if(optionalCount==0) return;
            if(useSinglePresenceWord(optionalCount)){
                sb.append(indent).append("long ").append(presenceVar).append("=").append(ioClass).append(".readPresenceBits(").append(bufVar).append(", ").append(optionalCount).append(");\n");
            }else{
                sb.append(indent).append("long[] ").append(presenceVar).append("=").append(ioClass).append(".readPresenceWords(").append(bufVar).append(", ").append(optionalCount).append(");\n");
            }
        }
        static String javaPresenceExpr(String presenceVar, int bitIndex, int optionalCount){
            if(useSinglePresenceWord(optionalCount)){
                return "(("+presenceVar+" & (1L << "+bitIndex+")) != 0L)";
            }
            return "BufUtil.isPresenceBitSet("+presenceVar+", "+bitIndex+")";
        }
        static void appendJavaReadAllPresentFields(StringBuilder sb, Struct s, String targetPrefix, String bufVar, String indent, boolean reuseExisting){
            appendJavaReadAllPresentFields(sb, s, targetPrefix, bufVar, indent, reuseExisting, s.hot);
        }
        static void appendJavaReadAllPresentFields(StringBuilder sb, Struct s, String targetPrefix, String bufVar, String indent, boolean reuseExisting, boolean hotMode){
            for(Field f: s.fields){
                String fieldExpr=targetPrefix+f.name;
                if(isPresenceTrackedType(f.type)){
                    if(isOptionalType(f.type)){
                        String inner=genericBody(f.type).trim();
                        if(reuseExisting && isJavaReusableReadTargetType(inner)){
                            String valueVar=childVar(fieldExpr, "value");
                            sb.append(indent).append(mapType(inner)).append(" ").append(valueVar).append("=")
                                    .append(optionalPresentExpr(fieldExpr)).append(" ? ").append(fieldExpr).append(".get() : null;\n");
                            appendJavaAssignReadExistingValue(sb, valueVar, inner, bufVar, indent, hotMode);
                            sb.append(indent).append(fieldExpr).append("=Optional.ofNullable(").append(valueVar).append(");\n");
                        }else{
                            appendJavaReadValueToLocal(sb, "__value", inner, bufVar, indent, hotMode);
                            sb.append(indent).append(fieldExpr).append("=Optional.ofNullable(__value);\n");
                        }
                    }else if(reuseExisting){
                        appendJavaAssignReadExistingValue(sb, fieldExpr, f, bufVar, indent, hotMode);
                    }else{
                        appendJavaAssignReadValue(sb, fieldExpr, f, bufVar, indent, hotMode);
                    }
                }else if(isOptionalType(f.type)){
                    String inner=genericBody(f.type).trim();
                    appendJavaReadValueToLocal(sb, "__value", inner, bufVar, indent, hotMode);
                    sb.append(indent).append(fieldExpr).append("=Optional.ofNullable(__value);\n");
                }else if(reuseExisting){
                    appendJavaAssignReadExistingValue(sb, fieldExpr, f, bufVar, indent, hotMode);
                }else{
                    appendJavaAssignReadValue(sb, fieldExpr, f, bufVar, indent, hotMode);
                }
            }
        }
        static void appendJavaReadDominantMaskFields(StringBuilder sb, Struct s, String targetPrefix, String bufVar, String indent, boolean reuseExisting, boolean hotMode, int presentTrackedCount){
            int presenceIndex=0;
            for(Field f: s.fields){
                String fieldExpr=targetPrefix+f.name;
                if(isPresenceTrackedType(f.type)){
                    boolean present=presenceIndex++<presentTrackedCount;
                    if(present){
                        if(isOptionalType(f.type)){
                            String inner=genericBody(f.type).trim();
                            if(reuseExisting && isJavaReusableReadTargetType(inner)){
                                String valueVar=childVar(fieldExpr, "value");
                                sb.append(indent).append(mapType(inner)).append(" ").append(valueVar).append("=")
                                        .append(optionalPresentExpr(fieldExpr)).append(" ? ").append(fieldExpr).append(".get() : null;\n");
                                appendJavaAssignReadExistingValue(sb, valueVar, inner, bufVar, indent, hotMode);
                                sb.append(indent).append(fieldExpr).append("=Optional.ofNullable(").append(valueVar).append(");\n");
                            }else{
                                appendJavaReadValueToLocal(sb, "__value", inner, bufVar, indent, hotMode);
                                sb.append(indent).append(fieldExpr).append("=Optional.ofNullable(__value);\n");
                            }
                        }else if(reuseExisting){
                            appendJavaAssignReadExistingValue(sb, fieldExpr, f, bufVar, indent, hotMode);
                        }else{
                            appendJavaAssignReadValue(sb, fieldExpr, f, bufVar, indent, hotMode);
                        }
                    }else{
                        if(reuseExisting){
                            appendJavaResetReadValue(sb, fieldExpr, f, indent);
                        }else{
                            sb.append(indent).append(fieldExpr).append("=").append(javaDefaultValueExpr(f)).append(";\n");
                        }
                    }
                }else if(isOptionalType(f.type)){
                    String inner=genericBody(f.type).trim();
                    appendJavaReadValueToLocal(sb, "__value", inner, bufVar, indent, hotMode);
                    sb.append(indent).append(fieldExpr).append("=Optional.ofNullable(__value);\n");
                }else if(reuseExisting){
                    appendJavaAssignReadExistingValue(sb, fieldExpr, f, bufVar, indent, hotMode);
                }else{
                    appendJavaAssignReadValue(sb, fieldExpr, f, bufVar, indent, hotMode);
                }
            }
        }
        static String javaProjectionIndicesExpr(Struct s, Field f){
            return isProjectionIndexedType(s, f)? "projection."+f.name+"Indices" : null;
        }
        static boolean isProjectionIndexedType(Struct s, Field f){
            if("byte[]".equals(f.type) || "Byte[]".equals(f.type)){
                return true;
            }
            if(isBorrowedPrimitiveArrayField(f)){
                return true;
            }
            return isFixedStruct(s) && isFixedSimdPrimitiveArray(f.type);
        }
        static void appendJavaReadProjectedAllPresentFields(StringBuilder sb, Struct s, String targetPrefix, String bufVar, String indent, boolean reuseExisting){
            appendJavaReadProjectedAllPresentFields(sb, s, targetPrefix, bufVar, indent, reuseExisting, s.hot);
        }
        static void appendJavaReadProjectedAllPresentFields(StringBuilder sb, Struct s, String targetPrefix, String bufVar, String indent, boolean reuseExisting, boolean hotMode){
            for(Field f: s.fields){
                String fieldExpr=targetPrefix+f.name;
                String selectedExpr="projection."+f.name;
                sb.append(indent).append("if(").append(selectedExpr).append("){\n");
                if(isBorrowProjectionField(f)){
                    appendJavaReadProjectedBorrowedValue(sb, fieldExpr, f, bufVar, indent+"    ", "projection."+f.name+"Limit", javaProjectionIndicesExpr(s, f));
                }else if(isPackedPrimitiveListField(f)){
                    appendJavaReadProjectedPackedListValue(sb, fieldExpr, f, bufVar, indent+"    ", "projection."+f.name+"Limit");
                }else if(fieldHasMetadataDrivenCodec(f)){
                    if(reuseExisting){
                        appendJavaAssignReadExistingValue(sb, fieldExpr, f, bufVar, indent+"    ", hotMode);
                    }else{
                        appendJavaAssignReadValue(sb, fieldExpr, f, bufVar, indent+"    ", hotMode);
                    }
                }else if(isProjectionLimitedType(f.type)){
                    appendJavaAssignProjectedReadValue(sb, fieldExpr, f.type, bufVar, indent+"    ", hotMode, "projection."+f.name+"Limit", javaProjectionIndicesExpr(s, f));
                }else if(reuseExisting){
                    appendJavaAssignReadExistingValue(sb, fieldExpr, f, bufVar, indent+"    ", hotMode);
                }else{
                    appendJavaAssignReadValue(sb, fieldExpr, f, bufVar, indent+"    ", hotMode);
                }
                sb.append(indent).append("}else{\n");
                appendJavaSkipValueStatements(sb, f, bufVar, indent+"    ", f.name);
                sb.append(indent).append("    ").append(fieldExpr).append("=").append(javaProjectionDefaultValueExpr(f.type)).append(";\n");
                sb.append(indent).append("}\n");
            }
        }
        static void appendJavaReadProjectedDominantMaskFields(StringBuilder sb, Struct s, String targetPrefix, String bufVar, String indent, boolean reuseExisting, boolean hotMode, int presentTrackedCount){
            int presenceIndex=0;
            for(Field f: s.fields){
                String fieldExpr=targetPrefix+f.name;
                String selectedExpr="projection."+f.name;
                if(isPresenceTrackedType(f.type)){
                    boolean present=presenceIndex++<presentTrackedCount;
                    if(present){
                        sb.append(indent).append("if(").append(selectedExpr).append("){\n");
                        if(isBorrowProjectionField(f)){
                            appendJavaReadProjectedBorrowedValue(sb, fieldExpr, f, bufVar, indent+"    ", "projection."+f.name+"Limit", javaProjectionIndicesExpr(s, f));
                        }else if(isPackedPrimitiveListField(f)){
                            appendJavaReadProjectedPackedListValue(sb, fieldExpr, f, bufVar, indent+"    ", "projection."+f.name+"Limit");
                        }else if(fieldHasMetadataDrivenCodec(f)){
                            if(reuseExisting){
                                appendJavaAssignReadExistingValue(sb, fieldExpr, f, bufVar, indent+"    ", hotMode);
                            }else{
                                appendJavaAssignReadValue(sb, fieldExpr, f, bufVar, indent+"    ", hotMode);
                            }
                        }else if(isProjectionLimitedType(f.type)){
                            appendJavaAssignProjectedReadValue(sb, fieldExpr, f.type, bufVar, indent+"    ", hotMode, "projection."+f.name+"Limit", javaProjectionIndicesExpr(s, f));
                        }else if(reuseExisting){
                            appendJavaAssignReadExistingValue(sb, fieldExpr, f, bufVar, indent+"    ", hotMode);
                        }else{
                            appendJavaAssignReadValue(sb, fieldExpr, f, bufVar, indent+"    ", hotMode);
                        }
                        sb.append(indent).append("}else{\n");
                        appendJavaSkipValueStatements(sb, f, bufVar, indent+"    ", f.name);
                        sb.append(indent).append("    ").append(fieldExpr).append("=").append(javaProjectionDefaultValueExpr(f.type)).append(";\n");
                        sb.append(indent).append("}\n");
                    }else{
                        if(reuseExisting){
                            appendJavaResetReadValue(sb, fieldExpr, f, indent);
                        }else{
                            sb.append(indent).append(fieldExpr).append("=").append(javaDefaultValueExpr(f)).append(";\n");
                        }
                    }
                }else{
                    sb.append(indent).append("if(").append(selectedExpr).append("){\n");
                    if(reuseExisting){
                        appendJavaAssignReadExistingValue(sb, fieldExpr, f, bufVar, indent+"    ", hotMode);
                    }else{
                        appendJavaAssignReadValue(sb, fieldExpr, f, bufVar, indent+"    ", hotMode);
                    }
                    sb.append(indent).append("}else{\n");
                    appendJavaSkipValueStatements(sb, f, bufVar, indent+"    ", f.name);
                    sb.append(indent).append("    ").append(fieldExpr).append("=").append(javaProjectionDefaultValueExpr(f.type)).append(";\n");
                    sb.append(indent).append("}\n");
                }
            }
        }
        static void appendJavaSkipDominantMaskFields(StringBuilder sb, Struct s, String bufVar, String indent, int presentTrackedCount){
            appendJavaSkipFieldSequence(sb, dominantMaskSkipFields(s, presentTrackedCount), bufVar, indent, false);
        }
        static void appendJavaWriteAllFields(StringBuilder sb, Struct s, String valuePrefix, String bufVar, String indent){
            appendJavaWriteAllFields(sb, s, valuePrefix, bufVar, indent, s.hot);
        }
        static void appendJavaWriteAllFields(StringBuilder sb, Struct s, String valuePrefix, String bufVar, String indent, boolean hotMode){
            for(Field f: s.fields){
                String fieldExpr=valuePrefix+f.name;
                if(isOptionalType(f.type)){
                    appendJavaWriteValueStatements(sb, fieldExpr+".get()", genericBody(f.type).trim(), bufVar, indent, hotMode);
                }else{
                    appendJavaWriteValueStatements(sb, fieldExpr, f, bufVar, indent, hotMode);
                }
            }
        }
        static void appendJavaProjectionType(StringBuilder sb, Struct s){
            sb.append("    public static final class Projection {\n");
            for(Field f: s.fields){
                sb.append("        public boolean ").append(f.name).append(";\n");
                if(isProjectionLimitedType(f.type)){
                    sb.append("        public int ").append(f.name).append("Limit=-1;\n");
                }
                if(isProjectionIndexedType(s, f)){
                    sb.append("        public int[] ").append(f.name).append("Indices;\n");
                }
            }
            sb.append("        public Projection selectAll(){\n");
            for(Field f: s.fields){
                sb.append("            this.").append(f.name).append("=true;\n");
            }
            sb.append("            return this;\n");
            sb.append("        }\n");
            sb.append("    }\n");
            sb.append("    public static Projection projection(){ return new Projection(); }\n");
            sb.append("    public static Projection fullProjection(){ return new Projection().selectAll(); }\n");
        }
        static void appendJavaReadFromMethodBody(StringBuilder sb, Struct s, List<Field> presenceFields, String bufType, String bufVar){
            boolean hotMode=s.hot;
            sb.append("    public static ").append(s.name).append(" readFrom(").append(bufType).append(" ").append(bufVar).append("){\n");
            sb.append("        ").append(s.name).append(" o=new ").append(s.name).append("();\n");
            appendJavaPresenceReadPrelude(sb, presenceFields.size(), bufVar, "        ", "ByteIO");
            if(useJavaFullPresenceFastPath(presenceFields.size())){
                sb.append("        if(__presence==").append(javaFullPresenceMaskLiteral(presenceFields.size())).append("){\n");
                appendJavaReadAllPresentFields(sb, s, "o.", bufVar, "            ", false, hotMode);
                sb.append("            return o;\n");
                sb.append("        }\n");
            }
            if(useJavaDominantMaskFamilies(presenceFields, hotMode)){
                List<Integer> dominantCounts=dominantMaskPresentCounts(presenceFields);
                for(int i=1;i<dominantCounts.size();i++){
                    int presentCount=dominantCounts.get(i);
                    sb.append("        if(__presence==").append(javaPresencePrefixMaskLiteral(presentCount)).append("){\n");
                    appendJavaReadDominantMaskFields(sb, s, "o.", bufVar, "            ", false, hotMode, presentCount);
                    sb.append("            return o;\n");
                    sb.append("        }\n");
                }
            }
            int presenceIndex=0;
            for(Field f: s.fields){
                if(isPresenceTrackedType(f.type)){
                    String presentExpr=javaPresenceExpr("__presence", presenceIndex++, presenceFields.size());
                    if(isOptionalType(f.type)){
                        String inner=genericBody(f.type).trim();
                        sb.append("        if(").append(presentExpr).append("){\n");
                        appendJavaReadValueToLocal(sb, "__value", inner, bufVar, "            ", hotMode);
                        sb.append("            o.").append(f.name).append("=Optional.ofNullable(__value);\n");
                        sb.append("        }else{\n");
                        sb.append("            o.").append(f.name).append("=Optional.empty();\n");
                        sb.append("        }\n");
                    }else{
                        sb.append("        if(").append(presentExpr).append("){\n");
                        appendJavaAssignReadValue(sb, "o."+f.name, f, bufVar, "            ", hotMode);
                        sb.append("        }else{\n");
                        sb.append("            o.").append(f.name).append("=").append(javaDefaultValueExpr(f)).append(";\n");
                        sb.append("        }\n");
                    }
                }else if(isOptionalType(f.type)){
                    String inner=genericBody(f.type).trim();
                    appendJavaReadValueToLocal(sb, "__value", inner, bufVar, "        ", hotMode);
                    sb.append("        o.").append(f.name).append("=Optional.ofNullable(__value);\n");
                }else{
                    appendJavaAssignReadValue(sb, "o."+f.name, f, bufVar, "        ", hotMode);
                }
            }
            sb.append("        return o;\n");
            sb.append("    }\n");
        }
        static void appendJavaWriteMethodBody(StringBuilder sb, Struct s, List<Field> presenceFields, String bufType, String bufVar){
            boolean hotMode=s.hot;
            sb.append("    @Override\n");
            sb.append("    public void writeTo(").append(bufType).append(" ").append(bufVar).append("){\n");
            sb.append("        if(ByteIO.shouldReserveOnWriteStart(").append(bufVar).append(")){\n");
            sb.append("            ByteIO.reserveForWriteStart(").append(bufVar).append(", estimatedSize());\n");
            sb.append("        }\n");
            if(useJavaFullPresenceFastPath(presenceFields.size())){
                sb.append("        if(").append(javaAllPresentExpr(presenceFields, "this.")).append("){\n");
                sb.append("            ByteIO.writePresenceBits(").append(bufVar).append(", ").append(javaFullPresenceMaskLiteral(presenceFields.size())).append(", ").append(presenceFields.size()).append(");\n");
                appendJavaWriteAllFields(sb, s, "this.", bufVar, "            ", hotMode);
                sb.append("            return;\n");
                sb.append("        }\n");
            }
            appendJavaPresenceWritePrelude(sb, presenceFields, "this.", bufVar, "        ", "ByteIO");
            for(Field f: s.fields){
                String fieldExpr="this."+f.name;
                if(isOptionalType(f.type)){
                    sb.append("        if(").append(optionalPresentExpr(fieldExpr)).append("){\n");
                    appendJavaWriteValueStatements(sb, fieldExpr+".get()", genericBody(f.type).trim(), bufVar, "            ", hotMode);
                    sb.append("        }\n");
                }else if(isPresenceTrackedType(f.type)){
                    sb.append("        if(").append(javaHasWireValueExpr(fieldExpr, f)).append("){\n");
                    appendJavaWriteValueStatements(sb, fieldExpr, f, bufVar, "            ", hotMode);
                    sb.append("        }\n");
                }else{
                    appendJavaWriteValueStatements(sb, fieldExpr, f, bufVar, "        ", hotMode);
                }
            }
            sb.append("    }\n");
        }
        static void appendJavaFixedReadFromMethodBody(StringBuilder sb, Struct s, String bufType, String bufVar){
            if("ByteBuf".equals(bufType) && SIMD_ENABLED && hasFixedSimdArrayField(s.fields)){
                sb.append("    public static ").append(s.name).append(" readFrom(").append(bufType).append(" ").append(bufVar)
                        .append("){ return readFrom(NettyCursor.threadLocal(").append(bufVar).append(")); }\n");
                return;
            }
            sb.append("    public static ").append(s.name).append(" readFrom(").append(bufType).append(" ").append(bufVar).append("){ ").append(s.name).append(" o=new ").append(s.name).append("(); readInto(").append(bufVar).append(", o); return o; }\n");
        }
        static void appendJavaFixedReadIntoMethodBody(StringBuilder sb, Struct s, String bufType, String bufVar){
            if("ByteBuf".equals(bufType) && SIMD_ENABLED && hasFixedSimdArrayField(s.fields)){
                sb.append("    public static void readInto(").append(bufType).append(" ").append(bufVar).append(", ").append(s.name)
                        .append(" o){ readInto(NettyCursor.threadLocal(").append(bufVar).append("), o); }\n");
                return;
            }
            boolean hotMode=s.hot;
            sb.append("    public static void readInto(").append(bufType).append(" ").append(bufVar).append(", ").append(s.name).append(" o){\n");
            sb.append("        if(o==null) throw new NullPointerException(\"target object can not be null\");\n");
            appendJavaFixedReadStructInto(sb, s, "o", bufVar, "        ", hotMode);
            sb.append("    }\n");
        }
        static void appendJavaFixedWriteMethodBody(StringBuilder sb, Struct s, String bufType, String bufVar){
            if("ByteBuf".equals(bufType) && SIMD_ENABLED && hasFixedSimdArrayField(s.fields)){
                sb.append("    @Override public void writeTo(").append(bufType).append(" ").append(bufVar)
                        .append("){ writeTo(NettyCursor.threadLocal(").append(bufVar).append(")); }\n");
                return;
            }
            boolean hotMode=s.hot;
            sb.append("    @Override public void writeTo(").append(bufType).append(" ").append(bufVar).append("){\n");
            sb.append("        if(ByteIO.shouldReserveOnWriteStart(").append(bufVar).append(")){\n");
            sb.append("            ByteIO.reserveForWriteStart(").append(bufVar).append(", estimatedSize());\n");
            sb.append("        }\n");
            appendJavaFixedWriteStructValue(sb, s, "this", bufVar, "        ", hotMode);
            sb.append("    }\n");
        }
        static void appendJavaProjectedReadMethodBody(StringBuilder sb, Struct s, List<Field> presenceFields){
            sb.append("    public static ").append(s.name).append(" readProjected(ByteBuf buf, Projection projection){\n");
            sb.append("        ").append(s.name).append(" o=new ").append(s.name).append("();\n");
            sb.append("        readProjectedInto(NettyCursor.threadLocal(buf), o, projection);\n");
            sb.append("        return o;\n");
            sb.append("    }\n");
            sb.append("    public static ").append(s.name).append(" readProjected(ByteCursor input, Projection projection){\n");
            sb.append("        ").append(s.name).append(" o=new ").append(s.name).append("();\n");
            sb.append("        readProjectedInto(input, o, projection);\n");
            sb.append("        return o;\n");
            sb.append("    }\n");
            sb.append("    public static void readProjectedInto(ByteBuf buf, ").append(s.name).append(" o, Projection projection){\n");
            sb.append("        readProjectedInto(NettyCursor.threadLocal(buf), o, projection);\n");
            sb.append("    }\n");
            sb.append("    public static void readProjectedInto(ByteCursor input, ").append(s.name).append(" o, Projection projection){\n");
            sb.append("        if(o==null) throw new NullPointerException(\"target object can not be null\");\n");
            sb.append("        if(projection==null){ readInto(input, o); return; }\n");
            appendJavaPresenceReadPrelude(sb, presenceFields.size(), "input", "        ", "ByteIO");
            if(useJavaFullPresenceFastPath(presenceFields.size())){
                sb.append("        if(__presence==").append(javaFullPresenceMaskLiteral(presenceFields.size())).append("){\n");
                appendJavaReadProjectedAllPresentFields(sb, s, "o.", "input", "            ", true, s.hot);
                sb.append("            return;\n");
                sb.append("        }\n");
            }
            if(useJavaDominantMaskFamilies(presenceFields, s.hot)){
                List<Integer> dominantCounts=dominantMaskPresentCounts(presenceFields);
                for(int i=1;i<dominantCounts.size();i++){
                    int presentCount=dominantCounts.get(i);
                    sb.append("        if(__presence==").append(javaPresencePrefixMaskLiteral(presentCount)).append("){\n");
                    appendJavaReadProjectedDominantMaskFields(sb, s, "o.", "input", "            ", true, s.hot, presentCount);
                    sb.append("            return;\n");
                    sb.append("        }\n");
                }
            }
            int presenceIndex=0;
            for(Field f: s.fields){
                String targetExpr="o."+f.name;
                String selectedExpr="projection."+f.name;
                if(isPresenceTrackedType(f.type)){
                    String presentExpr=javaPresenceExpr("__presence", presenceIndex++, presenceFields.size());
                    sb.append("        if(").append(presentExpr).append("){\n");
                    sb.append("            if(").append(selectedExpr).append("){\n");
                    if(isBorrowProjectionField(f)){
                        appendJavaReadProjectedBorrowedValue(sb, targetExpr, f, "input", "                ", "projection."+f.name+"Limit", javaProjectionIndicesExpr(s, f));
                    }else if(isPackedPrimitiveListField(f)){
                        appendJavaReadProjectedPackedListValue(sb, targetExpr, f, "input", "                ", "projection."+f.name+"Limit");
                    }else if(isPackedPrimitiveMapField(f) || isPackedIntKeyObjectMapField(f)){
                        appendJavaAssignReadExistingValue(sb, targetExpr, f, "input", "                ", s.hot);
                    }else if(fieldHasMetadataDrivenCodec(f)){
                        appendJavaAssignReadValue(sb, targetExpr, f, "input", "                ", s.hot);
                    }else if(isProjectionLimitedType(f.type)){
                        appendJavaAssignProjectedReadValue(sb, targetExpr, f.type, "input", "                ", s.hot, "projection."+f.name+"Limit", javaProjectionIndicesExpr(s, f));
                    }else{
                        appendJavaAssignReadValue(sb, targetExpr, f, "input", "                ", s.hot);
                    }
                    sb.append("            }else{\n");
                    appendJavaSkipValueStatements(sb, f, "input", "                ", f.name);
                    sb.append("                ").append(targetExpr).append("=").append(javaProjectionDefaultValueExpr(f.type)).append(";\n");
                    sb.append("            }\n");
                    sb.append("        }else if(").append(selectedExpr).append("){\n");
                    sb.append("            ").append(targetExpr).append("=").append(javaDefaultValueExpr(f)).append(";\n");
                    sb.append("        }else{\n");
                    sb.append("            ").append(targetExpr).append("=").append(javaProjectionDefaultValueExpr(f.type)).append(";\n");
                    sb.append("        }\n");
                }else{
                    sb.append("        if(").append(selectedExpr).append("){\n");
                    if(isBorrowProjectionField(f)){
                        appendJavaReadProjectedBorrowedValue(sb, targetExpr, f, "input", "            ", "projection."+f.name+"Limit", javaProjectionIndicesExpr(s, f));
                    }else if(isPackedPrimitiveListField(f)){
                        appendJavaReadProjectedPackedListValue(sb, targetExpr, f, "input", "            ", "projection."+f.name+"Limit");
                    }else if(isPackedPrimitiveMapField(f) || isPackedIntKeyObjectMapField(f)){
                        appendJavaAssignReadExistingValue(sb, targetExpr, f, "input", "            ", s.hot);
                    }else if(fieldHasMetadataDrivenCodec(f)){
                        appendJavaAssignReadValue(sb, targetExpr, f, "input", "            ", s.hot);
                    }else if(isProjectionLimitedType(f.type)){
                        appendJavaAssignProjectedReadValue(sb, targetExpr, f.type, "input", "            ", s.hot, "projection."+f.name+"Limit", javaProjectionIndicesExpr(s, f));
                    }else{
                        appendJavaAssignReadValue(sb, targetExpr, f, "input", "            ", s.hot);
                    }
                    sb.append("        }else{\n");
                    appendJavaSkipValueStatements(sb, f, "input", "            ", f.name);
                    sb.append("            ").append(targetExpr).append("=").append(javaProjectionDefaultValueExpr(f.type)).append(";\n");
                    sb.append("        }\n");
                }
            }
            sb.append("    }\n");
        }
        static void appendJavaSkipMethodBody(StringBuilder sb, Struct s, List<Field> presenceFields){
            sb.append("    public static void skip(ByteBuf buf){\n");
            sb.append("        skip(NettyCursor.threadLocal(buf));\n");
            sb.append("    }\n");
            sb.append("    public static void skip(ByteCursor input){\n");
            appendJavaPresenceReadPrelude(sb, presenceFields.size(), "input", "        ", "ByteIO");
            if(useJavaFullPresenceFastPath(presenceFields.size())){
                sb.append("        if(__presence==").append(javaFullPresenceMaskLiteral(presenceFields.size())).append("){\n");
                appendJavaSkipFieldSequence(sb, s.fields, "input", "            ", false);
                sb.append("            return;\n");
                sb.append("        }\n");
            }
            if(useJavaDominantMaskFamilies(presenceFields, s.hot)){
                List<Integer> dominantCounts=dominantMaskPresentCounts(presenceFields);
                for(int i=1;i<dominantCounts.size();i++){
                    int presentCount=dominantCounts.get(i);
                    sb.append("        if(__presence==").append(javaPresencePrefixMaskLiteral(presentCount)).append("){\n");
                    appendJavaSkipDominantMaskFields(sb, s, "input", "            ", presentCount);
                    sb.append("            return;\n");
                    sb.append("        }\n");
                }
            }
            int presenceIndex=0;
            for(Field f: s.fields){
                if(isPresenceTrackedType(f.type)){
                    String presentExpr=javaPresenceExpr("__presence", presenceIndex++, presenceFields.size());
                    sb.append("        if(").append(presentExpr).append("){\n");
                    appendJavaSkipValueStatements(sb, f, "input", "            ", f.name);
                    sb.append("        }\n");
                }else{
                    appendJavaSkipValueStatements(sb, f, "input", "        ", f.name);
                }
            }
            sb.append("    }\n");
        }
        static void appendJavaProjectedFixedReadMethodBody(StringBuilder sb, Struct s){
            sb.append("    public static ").append(s.name).append(" readProjected(ByteBuf buf, Projection projection){ ").append(s.name).append(" o=new ").append(s.name).append("(); readProjectedInto(NettyCursor.threadLocal(buf), o, projection); return o; }\n");
            sb.append("    public static ").append(s.name).append(" readProjected(ByteCursor input, Projection projection){\n");
            sb.append("        ").append(s.name).append(" o=new ").append(s.name).append("();\n");
            sb.append("        readProjectedInto(input, o, projection);\n");
            sb.append("        return o;\n");
            sb.append("    }\n");
            sb.append("    public static void readProjectedInto(ByteBuf buf, ").append(s.name).append(" o, Projection projection){ readProjectedInto(NettyCursor.threadLocal(buf), o, projection); }\n");
            sb.append("    public static void readProjectedInto(ByteCursor input, ").append(s.name).append(" o, Projection projection){\n");
            sb.append("        if(o==null) throw new NullPointerException(\"target object can not be null\");\n");
            sb.append("        if(projection==null){ readInto(input, o); return; }\n");
            for(Field f: s.fields){
                String targetExpr="o."+f.name;
                String selectedExpr="projection."+f.name;
                sb.append("        if(").append(selectedExpr).append("){\n");
                if(isBorrowProjectionField(f)){
                    appendJavaReadProjectedBorrowedValue(sb, targetExpr, f, "input", "            ", "projection."+f.name+"Limit", javaProjectionIndicesExpr(s, f));
                }else if(fieldHasMetadataDrivenCodec(f)){
                    appendJavaAssignReadValue(sb, targetExpr, f, "input", "            ", true);
                }else if(isProjectionLimitedType(f.type)){
                    appendJavaAssignProjectedReadValue(sb, targetExpr, f.type, "input", "            ", true, "projection."+f.name+"Limit", javaProjectionIndicesExpr(s, f));
                }else{
                    appendJavaFixedReadField(sb, targetExpr, f, "input", "            ", true);
                }
                sb.append("        }else{\n");
                if(fieldHasMetadataDrivenCodec(f)){
                    appendJavaSkipValueStatements(sb, f, "input", "            ", f.name);
                }else{
                    appendJavaSkipFixedValueStatements(sb, f.type, "input", "            ", f.name);
                }
                sb.append("            ").append(targetExpr).append("=").append(javaProjectionDefaultValueExpr(f.type)).append(";\n");
                sb.append("        }\n");
            }
            sb.append("    }\n");
        }
        static void appendJavaAssignProjectedReadValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean hot, String limitExpr, String indicesExpr){
            if(t.endsWith("[]")){
                appendJavaReadProjectedArrayValue(sb, targetExpr, t, bufVar, indent, hot, limitExpr, indicesExpr);
                return;
            }
            if(isListLikeType(t)){
                appendJavaReadProjectedListValue(sb, targetExpr, t, bufVar, indent, hot, limitExpr);
                return;
            }
            if(isSetLikeType(t)){
                appendJavaReadProjectedSetValue(sb, targetExpr, t, bufVar, indent, hot, limitExpr);
                return;
            }
            if(isQueueLikeType(t)){
                appendJavaReadProjectedQueueValue(sb, targetExpr, t, bufVar, indent, hot, limitExpr);
                return;
            }
            if(isMapLikeType(t)){
                appendJavaReadProjectedMapValue(sb, targetExpr, t, bufVar, indent, hot, limitExpr);
                return;
            }
            appendJavaAssignReadExistingValue(sb, targetExpr, t, bufVar, indent, hot);
        }
        static void appendJavaReadProjectedArrayValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean hot, String limitExpr, String indicesExpr){
            String inner=t.substring(0, t.length()-2).trim();
            String countVar=childVar(targetExpr, "count");
            String limitVar=childVar(targetExpr, "limit");
            String readCountVar=childVar(targetExpr, "readCount");
            String indexVar=childVar(targetExpr, "index");
            String reuseVar=childVar(targetExpr, "reuse");
            String indicesVar=childVar(targetExpr, "indices");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            if(indicesExpr!=null){
                sb.append(indent).append("    int[] ").append(indicesVar).append("=").append(indicesExpr).append(";\n");
                sb.append(indent).append("    if(").append(indicesVar).append("!=null){\n");
                sb.append(indent).append("        ").append(mapType(t)).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                if(inner.equals("byte")){
                    sb.append(indent).append("        ").append(targetExpr).append("=ByteIO.readSampledBytes(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(", ").append(indicesVar).append(");\n");
                }else if(inner.equals("int")){
                    sb.append(indent).append("        ").append(targetExpr).append("=ByteIO.readSampledFixedIntArray(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(", ").append(indicesVar).append(");\n");
                }else if(inner.equals("long")){
                    sb.append(indent).append("        ").append(targetExpr).append("=ByteIO.readSampledFixedLongArray(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(", ").append(indicesVar).append(");\n");
                }else if(inner.equals("float")){
                    sb.append(indent).append("        ").append(targetExpr).append("=ByteIO.readSampledFixedFloatArray(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(", ").append(indicesVar).append(");\n");
                }else if(inner.equals("double")){
                    sb.append(indent).append("        ").append(targetExpr).append("=ByteIO.readSampledFixedDoubleArray(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(", ").append(indicesVar).append(");\n");
                }else{
                    sb.append(indent).append("        ").append(targetExpr).append("=").append(javaArrayAllocationExpr(t, "0")).append(";\n");
                    sb.append(indent).append("        ").append(bufVar).append(".skip(").append(countVar).append(");\n");
                }
                sb.append(indent).append("    }else{\n");
            }
            sb.append(indent).append("    int ").append(limitVar).append("=").append(limitExpr).append(";\n");
            sb.append(indent).append("    int ").append(readCountVar).append("=").append(limitVar).append("<0 ? ").append(countVar)
                    .append(" : Math.min(").append(countVar).append(", Math.max(").append(limitVar).append(", 0));\n");
            if(SIMD_ENABLED && hot && isPrimitive(inner) && !inner.equals("boolean") && !inner.equals("char") && !inner.equals("short")){
                sb.append(indent).append("    if(").append(readCountVar).append("==").append(countVar).append("){\n");
                sb.append(indent).append("        ").append(mapType(t)).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                sb.append(indent).append("        if(").append(countVar).append("==0){\n");
                sb.append(indent).append("            ").append(targetExpr).append("=").append(javaArrayAllocationExpr(t, "0")).append(";\n");
                sb.append(indent).append("        }else{\n");
                sb.append(indent).append("            if(").append(reuseVar).append("==null || ").append(reuseVar).append(".length!=").append(countVar).append("){\n");
                sb.append(indent).append("                ").append(reuseVar).append("=").append(javaArrayAllocationExpr(t, countVar)).append(";\n");
                sb.append(indent).append("                ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("            }\n");
                if(inner.equals("byte")){
                    sb.append(indent).append("            ByteIO.readRawByteArray(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(");\n");
                }else if(inner.equals("int")){
                    sb.append(indent).append("            ByteIO.readRawIntArray(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(");\n");
                }else if(inner.equals("long")){
                    sb.append(indent).append("            ByteIO.readRawLongArray(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(");\n");
                }else if(inner.equals("float")){
                    sb.append(indent).append("            ByteIO.readRawFloatArray(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(");\n");
                }else if(inner.equals("double")){
                    sb.append(indent).append("            ByteIO.readRawDoubleArray(").append(bufVar).append(", ").append(reuseVar).append(", ").append(countVar).append(");\n");
                }else{
                    sb.append(indent).append("            for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                    appendJavaAssignReadExistingValue(sb, reuseVar+"["+indexVar+"]", inner, bufVar, indent+"                ", hot);
                    sb.append(indent).append("            }\n");
                }
                sb.append(indent).append("        }\n");
                sb.append(indent).append("    }else{\n");
            }
            if(inner.equals("byte")){
                sb.append(indent).append("    byte[] ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                sb.append(indent).append("    if(").append(readCountVar).append("==0){\n");
                sb.append(indent).append("        ").append(targetExpr).append("=new byte[0];\n");
                sb.append(indent).append("    }else{\n");
                sb.append(indent).append("        if(").append(reuseVar).append("==null || ").append(reuseVar).append(".length!=").append(readCountVar).append("){\n");
                sb.append(indent).append("            ").append(reuseVar).append("=new byte[").append(readCountVar).append("];\n");
                sb.append(indent).append("            ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("        ").append(bufVar).append(".readBytes(").append(reuseVar).append(",0,").append(readCountVar).append(");\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("    if(").append(countVar).append(">").append(readCountVar).append(") ").append(bufVar).append(".skip(").append(countVar).append("-").append(readCountVar).append(");\n");
            }else{
                sb.append(indent).append("    ").append(mapType(t)).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                sb.append(indent).append("    if(").append(readCountVar).append("==0){\n");
                sb.append(indent).append("        ").append(targetExpr).append("=").append(javaArrayAllocationExpr(t, "0")).append(";\n");
                sb.append(indent).append("    }else{\n");
                sb.append(indent).append("        if(").append(reuseVar).append("==null || ").append(reuseVar).append(".length!=").append(readCountVar).append("){\n");
                sb.append(indent).append("            ").append(reuseVar).append("=").append(javaArrayAllocationExpr(t, readCountVar)).append(";\n");
                sb.append(indent).append("            ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("        for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(readCountVar).append(";").append(indexVar).append("++){\n");
                appendJavaAssignReadExistingValue(sb, reuseVar+"["+indexVar+"]", inner, bufVar, indent+"            ", hot);
                sb.append(indent).append("        }\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("    for(int ").append(indexVar).append("=").append(readCountVar).append(";").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendJavaSkipValueStatements(sb, inner, bufVar, indent+"        ", targetExpr+"Elem");
                sb.append(indent).append("    }\n");
            }
            if(SIMD_ENABLED && hot && isPrimitive(inner) && !inner.equals("boolean") && !inner.equals("char") && !inner.equals("short")){
                sb.append(indent).append("    }\n");
            }
            if(indicesExpr!=null){
                sb.append(indent).append("    }\n");
            }
            sb.append(indent).append("}\n");
        }
        static void appendJavaReadProjectedListValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean hot, String limitExpr){
            String inner=genericBody(t).trim();
            String countVar=childVar(targetExpr, "count");
            String limitVar=childVar(targetExpr, "limit");
            String readCountVar=childVar(targetExpr, "readCount");
            String indexVar=childVar(targetExpr, "index");
            String reuseVar=childVar(targetExpr, "reuse");
            String existingCountVar=childVar(targetExpr, "existingCount");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    int ").append(limitVar).append("=").append(limitExpr).append(";\n");
            if(hot){
                sb.append(indent).append("    if(").append(limitVar).append("==1){\n");
                if("LinkedList".equals(canonicalContainerType(t))){
                    sb.append(indent).append("        LinkedList<").append(mapType(inner)).append("> ").append(reuseVar).append("=(").append(targetExpr).append(" instanceof LinkedList) ? (LinkedList<").append(mapType(inner)).append(">)").append(targetExpr).append(" : new LinkedList<>();\n");
                    sb.append(indent).append("        ").append(reuseVar).append(".clear();\n");
                    sb.append(indent).append("        ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                    sb.append(indent).append("        if(").append(countVar).append("!=0){\n");
                    appendJavaReadValueToLocal(sb, "__value", inner, bufVar, indent+"            ", hot);
                    sb.append(indent).append("            ").append(reuseVar).append(".add(__value);\n");
                    sb.append(indent).append("        }\n");
                }else{
                    sb.append(indent).append("        List<").append(mapType(inner)).append("> ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                    sb.append(indent).append("        if(").append(reuseVar).append("==null){\n");
                    sb.append(indent).append("            ").append(reuseVar).append("=ByteIO.borrowArrayList(").append(countVar).append("==0 ? 0 : 1);\n");
                    sb.append(indent).append("            ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                    sb.append(indent).append("        }\n");
                    sb.append(indent).append("        if(").append(reuseVar).append(" instanceof ArrayList){\n");
                    sb.append(indent).append("            ((ArrayList<").append(mapType(inner)).append(">)").append(reuseVar).append(").ensureCapacity(").append(countVar).append("==0 ? 0 : 1);\n");
                    sb.append(indent).append("        }\n");
                    sb.append(indent).append("        int ").append(existingCountVar).append("=").append(reuseVar).append(".size();\n");
                    sb.append(indent).append("        if(").append(countVar).append("==0){\n");
                    sb.append(indent).append("            if(").append(existingCountVar).append("!=0){ ").append(reuseVar).append(".clear(); }\n");
                    sb.append(indent).append("        }else{\n");
                    sb.append(indent).append("            if(").append(existingCountVar).append("!=0){\n");
                    if(isJavaReusableReadTargetType(inner)){
                        sb.append(indent).append("                ").append(mapType(inner)).append(" __value=").append(reuseVar).append(".get(0);\n");
                        appendJavaAssignReadExistingValue(sb, "__value", inner, bufVar, indent+"                ", hot);
                        sb.append(indent).append("                ").append(reuseVar).append(".set(0, __value);\n");
                    }else{
                        appendJavaReadValueToLocal(sb, "__value", inner, bufVar, indent+"                ", hot);
                        sb.append(indent).append("                ").append(reuseVar).append(".set(0, __value);\n");
                    }
                    sb.append(indent).append("                if(").append(existingCountVar).append(">1){\n");
                    sb.append(indent).append("                    ").append(reuseVar).append(".subList(1, ").append(existingCountVar).append(").clear();\n");
                    sb.append(indent).append("                }\n");
                    sb.append(indent).append("            }else{\n");
                    appendJavaReadValueToLocal(sb, "__value", inner, bufVar, indent+"                ", hot);
                    sb.append(indent).append("                ").append(reuseVar).append(".add(__value);\n");
                    sb.append(indent).append("            }\n");
                    sb.append(indent).append("        }\n");
                }
                if(isJavaHotTailSkipStruct(inner)){
                    sb.append(indent).append("        if(").append(countVar).append(">1) ").append(inner).append(".skipMany(").append(bufVar).append(", ").append(countVar).append("-1);\n");
                }else{
                    sb.append(indent).append("        for(int ").append(indexVar).append("=1;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                    appendJavaSkipValueStatements(sb, inner, bufVar, indent+"            ", targetExpr+"Elem");
                    sb.append(indent).append("        }\n");
                }
                sb.append(indent).append("    }else{\n");
            }
            sb.append(indent).append("    int ").append(readCountVar).append("=").append(limitVar).append("<0 ? ").append(countVar)
                    .append(" : Math.min(").append(countVar).append(", Math.max(").append(limitVar).append(", 0));\n");
            if("LinkedList".equals(canonicalContainerType(t))){
                sb.append(indent).append("    LinkedList<").append(mapType(inner)).append("> ").append(reuseVar).append("=(").append(targetExpr).append(" instanceof LinkedList) ? (LinkedList<").append(mapType(inner)).append(">)").append(targetExpr).append(" : new LinkedList<>();\n");
                sb.append(indent).append("    ").append(reuseVar).append(".clear();\n");
                sb.append(indent).append("    ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(readCountVar).append(";").append(indexVar).append("++){\n");
                appendJavaReadValueToLocal(sb, "__value", inner, bufVar, indent+"        ", hot);
                sb.append(indent).append("        ").append(reuseVar).append(".add(__value);\n");
                sb.append(indent).append("    }\n");
            }else{
                sb.append(indent).append("    List<").append(mapType(inner)).append("> ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                sb.append(indent).append("    if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("        ").append(reuseVar).append("=ByteIO.borrowArrayList(").append(readCountVar).append(");\n");
                sb.append(indent).append("        ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("    if(").append(reuseVar).append(" instanceof ArrayList){\n");
                sb.append(indent).append("        ((ArrayList<").append(mapType(inner)).append(">)").append(reuseVar).append(").ensureCapacity(").append(readCountVar).append(");\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("    int ").append(existingCountVar).append("=").append(reuseVar).append(".size();\n");
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(readCountVar).append(";").append(indexVar).append("++){\n");
                sb.append(indent).append("        if(").append(indexVar).append("<").append(existingCountVar).append("){\n");
                if(isJavaReusableReadTargetType(inner)){
                    sb.append(indent).append("            ").append(mapType(inner)).append(" __value=").append(reuseVar).append(".get(").append(indexVar).append(");\n");
                    appendJavaAssignReadExistingValue(sb, "__value", inner, bufVar, indent+"            ", hot);
                    sb.append(indent).append("            ").append(reuseVar).append(".set(").append(indexVar).append(", __value);\n");
                }else{
                    appendJavaReadValueToLocal(sb, "__value", inner, bufVar, indent+"            ", hot);
                    sb.append(indent).append("            ").append(reuseVar).append(".set(").append(indexVar).append(", __value);\n");
                }
                sb.append(indent).append("        }else{\n");
                appendJavaReadValueToLocal(sb, "__value", inner, bufVar, indent+"            ", hot);
                sb.append(indent).append("            ").append(reuseVar).append(".add(__value);\n");
                sb.append(indent).append("        }\n");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("    if(").append(existingCountVar).append(">").append(readCountVar).append("){\n");
                sb.append(indent).append("        ").append(reuseVar).append(".subList(").append(readCountVar).append(", ").append(existingCountVar).append(").clear();\n");
                sb.append(indent).append("    }\n");
            }
            if(isJavaHotTailSkipStruct(inner)){
                sb.append(indent).append("    if(").append(countVar).append(">").append(readCountVar).append(") ").append(inner).append(".skipMany(").append(bufVar).append(", ").append(countVar).append("-").append(readCountVar).append(");\n");
            }else{
                sb.append(indent).append("    for(int ").append(indexVar).append("=").append(readCountVar).append(";").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendJavaSkipValueStatements(sb, inner, bufVar, indent+"        ", targetExpr+"Elem");
                sb.append(indent).append("    }\n");
            }
            if(hot){
                sb.append(indent).append("    }\n");
            }
            sb.append(indent).append("}\n");
        }
        static void appendJavaReadProjectedSetValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean hot, String limitExpr){
            String inner=genericBody(t).trim();
            String countVar=childVar(targetExpr, "count");
            String limitVar=childVar(targetExpr, "limit");
            String readCountVar=childVar(targetExpr, "readCount");
            String indexVar=childVar(targetExpr, "index");
            String reuseVar=childVar(targetExpr, "reuse");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    int ").append(limitVar).append("=").append(limitExpr).append(";\n");
            sb.append(indent).append("    int ").append(readCountVar).append("=").append(limitVar).append("<0 ? ").append(countVar)
                    .append(" : Math.min(").append(countVar).append(", Math.max(").append(limitVar).append(", 0));\n");
            if("LinkedHashSet".equals(canonicalContainerType(t))){
                sb.append(indent).append("    LinkedHashSet<").append(mapType(inner)).append("> ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                sb.append(indent).append("    if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("        ").append(reuseVar).append("=ByteIO.borrowLinkedHashSet(").append(readCountVar).append(");\n");
                sb.append(indent).append("        ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("    }else{\n");
                sb.append(indent).append("        ").append(reuseVar).append(".clear();\n");
                sb.append(indent).append("    }\n");
            }else{
                sb.append(indent).append("    Set<").append(mapType(inner)).append("> ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                sb.append(indent).append("    if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("        ").append(reuseVar).append("=ByteIO.borrowHashSet(").append(readCountVar).append(");\n");
                sb.append(indent).append("        ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("    }else{\n");
                sb.append(indent).append("        ").append(reuseVar).append(".clear();\n");
                sb.append(indent).append("    }\n");
            }
            sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(readCountVar).append(";").append(indexVar).append("++){\n");
            appendJavaReadValueToLocal(sb, "__value", inner, bufVar, indent+"        ", hot);
            sb.append(indent).append("        ").append(reuseVar).append(".add(__value);\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("    for(int ").append(indexVar).append("=").append(readCountVar).append(";").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            appendJavaSkipValueStatements(sb, inner, bufVar, indent+"        ", targetExpr+"Elem");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaReadProjectedQueueValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean hot, String limitExpr){
            String inner=genericBody(t).trim();
            String queueType=mapType(t);
            String countVar=childVar(targetExpr, "count");
            String limitVar=childVar(targetExpr, "limit");
            String readCountVar=childVar(targetExpr, "readCount");
            String indexVar=childVar(targetExpr, "index");
            String reuseVar=childVar(targetExpr, "reuse");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    int ").append(limitVar).append("=").append(limitExpr).append(";\n");
            sb.append(indent).append("    int ").append(readCountVar).append("=").append(limitVar).append("<0 ? ").append(countVar)
                    .append(" : Math.min(").append(countVar).append(", Math.max(").append(limitVar).append(", 0));\n");
            sb.append(indent).append("    ").append(queueType).append(" ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("    if(").append(reuseVar).append("==null){\n");
            sb.append(indent).append("        ").append(reuseVar).append("=ByteIO.borrowArrayDeque(").append(readCountVar).append(");\n");
            sb.append(indent).append("        ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("    }else{\n");
                sb.append(indent).append("        ").append(reuseVar).append(".clear();\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(readCountVar).append(";").append(indexVar).append("++){\n");
            appendJavaReadValueToLocal(sb, "__value", inner, bufVar, indent+"        ", hot);
            sb.append(indent).append("        ").append(reuseVar).append(".add(__value);\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("    for(int ").append(indexVar).append("=").append(readCountVar).append(";").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            appendJavaSkipValueStatements(sb, inner, bufVar, indent+"        ", targetExpr+"Elem");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaReadProjectedMapValue(StringBuilder sb, String targetExpr, String t, String bufVar, String indent, boolean hot, String limitExpr){
            List<String> kv=splitTopLevel(genericBody(t), ',');
            String keyType=kv.get(0).trim();
            String valueType=kv.get(1).trim();
            String countVar=childVar(targetExpr, "count");
            String limitVar=childVar(targetExpr, "limit");
            String readCountVar=childVar(targetExpr, "readCount");
            String indexVar=childVar(targetExpr, "index");
            String reuseVar=childVar(targetExpr, "reuse");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    int ").append(limitVar).append("=").append(limitExpr).append(";\n");
            sb.append(indent).append("    int ").append(readCountVar).append("=").append(limitVar).append("<0 ? ").append(countVar)
                    .append(" : Math.min(").append(countVar).append(", Math.max(").append(limitVar).append(", 0));\n");
            if("LinkedHashMap".equals(canonicalContainerType(t))){
                sb.append(indent).append("    LinkedHashMap<").append(mapType(keyType)).append(",").append(mapType(valueType)).append("> ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                sb.append(indent).append("    if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("        ").append(reuseVar).append("=ByteIO.borrowLinkedHashMap(").append(readCountVar).append(");\n");
                sb.append(indent).append("        ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("    }else{\n");
                sb.append(indent).append("        ").append(reuseVar).append(".clear();\n");
                sb.append(indent).append("    }\n");
            }else{
                sb.append(indent).append("    Map<").append(mapType(keyType)).append(",").append(mapType(valueType)).append("> ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                sb.append(indent).append("    if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("        ").append(reuseVar).append("=ByteIO.borrowHashMap(").append(readCountVar).append(");\n");
                sb.append(indent).append("        ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("    }else{\n");
                sb.append(indent).append("        ").append(reuseVar).append(".clear();\n");
                sb.append(indent).append("    }\n");
            }
            sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(readCountVar).append(";").append(indexVar).append("++){\n");
            appendJavaReadValueToLocal(sb, "__key", keyType, bufVar, indent+"        ", hot);
            appendJavaReadValueToLocal(sb, "__value", valueType, bufVar, indent+"        ", hot);
            sb.append(indent).append("        ").append(reuseVar).append(".put(__key, __value);\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("    for(int ").append(indexVar).append("=").append(readCountVar).append(";").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            appendJavaSkipValueStatements(sb, keyType, bufVar, indent+"        ", targetExpr+"Key");
            appendJavaSkipValueStatements(sb, valueType, bufVar, indent+"        ", targetExpr+"Value");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static void appendJavaSkipManyMethodBody(StringBuilder sb, Struct s){
            sb.append("    public static void skipMany(ByteBuf buf, int count){\n");
            sb.append("        skipMany(NettyCursor.threadLocal(buf), count);\n");
            sb.append("    }\n");
            sb.append("    public static void skipMany(ByteCursor input, int count){\n");
            sb.append("        for(int __skipManyIndex=0;__skipManyIndex<count;__skipManyIndex++){\n");
            sb.append("            skip(input);\n");
            sb.append("        }\n");
            sb.append("    }\n");
        }
        static void appendJavaSkipFixedMethodBody(StringBuilder sb, Struct s){
            sb.append("    public static void skip(ByteBuf buf){ skip(NettyCursor.threadLocal(buf)); }\n");
            sb.append("    public static void skip(ByteCursor input){\n");
            appendJavaSkipFieldSequence(sb, s.fields, "input", "        ", true);
            sb.append("    }\n");
        }
        static void appendJavaSkipValueStatements(StringBuilder sb, Field f, String bufVar, String indent, String scopeKey){
            if(isBorrowedBytesField(f)){
                if(f.fixedLength!=null){
                    sb.append(indent).append(bufVar).append(".skip(").append(f.fixedLength).append(");\n");
                }else{
                    sb.append(indent).append("ByteIO.skipBytes(").append(bufVar).append(");\n");
                }
                return;
            }
            if(isBorrowedStringField(f) || isFixedLengthStringField(f)){
                if(f.fixedLength!=null){
                    sb.append(indent).append(bufVar).append(".skip(").append(f.fixedLength).append(");\n");
                }else{
                    sb.append(indent).append("ByteIO.skipString(").append(bufVar).append(");\n");
                }
                return;
            }
            if(isBorrowedPrimitiveArrayField(f)){
                int scalarBytes=javaFixedSkipScalarBytes(f.type.substring(0, f.type.length()-2).trim());
                if(f.fixedLength!=null){
                    sb.append(indent).append("if(").append(f.fixedLength).append("!=0) ").append(bufVar).append(".skip(")
                            .append(f.fixedLength).append("*").append(scalarBytes).append(");\n");
                }else{
                    String countVar=childVar(scopeKey, "skipCount");
                    sb.append(indent).append("{\n");
                    sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
                    sb.append(indent).append("    if(").append(countVar).append("!=0) ").append(bufVar).append(".skip(")
                            .append(countVar).append("*").append(scalarBytes).append(");\n");
                    sb.append(indent).append("}\n");
                }
                return;
            }
            if(isFixedCountArrayField(f)){
                String inner=f.type.substring(0, f.type.length()-2).trim();
                int scalarBytes="byte".equals(inner)?1:javaFixedSkipScalarBytes(inner);
                if(scalarBytes<=0){
                    throw new IllegalArgumentException("unsupported @fixed array skip type: "+f.type);
                }
                sb.append(indent).append("if(").append(f.fixedLength).append("!=0) ").append(bufVar).append(".skip(")
                        .append(f.fixedLength).append("*").append(scalarBytes).append(");\n");
                return;
            }
            if(isPackedPrimitiveListField(f)){
                String inner=genericBody(f.type).trim();
                int scalarBytes=javaFixedSkipScalarBytes(inner);
                String countVar=childVar(scopeKey, "skipCount");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
                sb.append(indent).append("    if(").append(countVar).append("!=0) ").append(bufVar).append(".skip(")
                        .append(countVar).append("*").append(scalarBytes).append(");\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isPackedPrimitiveMapField(f)){
                List<String> kv=splitTopLevel(genericBody(f.type), ',');
                int keyBytes=javaFixedSkipScalarBytes(kv.get(0).trim());
                int valueBytes=javaFixedSkipScalarBytes(kv.get(1).trim());
                String countVar=childVar(scopeKey, "skipCount");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
                sb.append(indent).append("    if(").append(countVar).append("!=0) ").append(bufVar).append(".skip(")
                        .append(countVar).append("*").append(keyBytes+valueBytes).append(");\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(isPackedIntKeyObjectMapField(f)){
                List<String> kv=splitTopLevel(genericBody(f.type), ',');
                String valueType=kv.get(1).trim();
                String countVar=childVar(scopeKey, "skipCount");
                String indexVar=childVar(scopeKey, "skipIndex");
                sb.append(indent).append("{\n");
                sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                sb.append(indent).append("        ").append(bufVar).append(".skip(4);\n");
                appendJavaSkipValueStatements(sb, valueType, bufVar, indent+"        ", scopeKey+"PackedValue");
                sb.append(indent).append("    }\n");
                sb.append(indent).append("}\n");
                return;
            }
            appendJavaSkipValueStatements(sb, f.type, bufVar, indent, scopeKey);
        }
        static void appendJavaSkipValueStatements(StringBuilder sb, String t, String bufVar, String indent, String scopeKey){
            if(t.equals("int") || t.equals("Integer") || t.equals("long") || t.equals("Long")
                    || t.equals("byte") || t.equals("Byte") || t.equals("short") || t.equals("Short")
                    || t.equals("boolean") || t.equals("Boolean") || t.equals("char") || t.equals("Character")
                    || t.equals("float") || t.equals("Float") || t.equals("double") || t.equals("Double")){
                if(t.equals("int") || t.equals("Integer")){
                    sb.append(indent).append("ByteIO.skipInt(").append(bufVar).append(");\n");
                }else if(t.equals("long") || t.equals("Long")){
                    sb.append(indent).append("ByteIO.skipLong(").append(bufVar).append(");\n");
                }else if(t.equals("byte") || t.equals("Byte")){
                    sb.append(indent).append("ByteIO.skipByte(").append(bufVar).append(");\n");
                }else if(t.equals("short") || t.equals("Short")){
                    sb.append(indent).append("ByteIO.skipShort(").append(bufVar).append(");\n");
                }else if(t.equals("boolean") || t.equals("Boolean")){
                    sb.append(indent).append("ByteIO.skipBoolean(").append(bufVar).append(");\n");
                }else if(t.equals("char") || t.equals("Character")){
                    sb.append(indent).append("ByteIO.skipChar(").append(bufVar).append(");\n");
                }else if(t.equals("float") || t.equals("Float")){
                    sb.append(indent).append("ByteIO.skipFloat(").append(bufVar).append(");\n");
                }else{
                    sb.append(indent).append("ByteIO.skipDouble(").append(bufVar).append(");\n");
                }
                return;
            }
            if(t.equals("String")){
                sb.append(indent).append("ByteIO.skipString(").append(bufVar).append(");\n");
                return;
            }
            if(ENUMS.contains(t)){
                sb.append(indent).append("ByteIO.skipUInt(").append(bufVar).append(");\n");
                return;
            }
            if(isOptionalType(t)){
                appendJavaSkipValueStatements(sb, genericBody(t).trim(), bufVar, indent, scopeKey+"Opt");
                return;
            }
            if(t.endsWith("[]")){
                appendJavaSkipArrayValueStatements(sb, t, bufVar, indent, false, scopeKey);
                return;
            }
            if(isListLikeType(t) || isSetLikeType(t) || isQueueLikeType(t)){
                appendJavaSkipCollectionValueStatements(sb, genericBody(t).trim(), bufVar, indent, false, scopeKey);
                return;
            }
            if(isMapLikeType(t)){
                List<String> kv=splitTopLevel(genericBody(t), ',');
                appendJavaSkipMapValueStatements(sb, kv.get(0).trim(), kv.get(1).trim(), bufVar, indent, false, scopeKey);
                return;
            }
            if(isStructType(t)){
                sb.append(indent).append(t).append(".skip(").append(bufVar).append(");\n");
                return;
            }
            sb.append(indent).append(readCursorValue(bufVar, t)).append(";\n");
        }
        static void appendJavaSkipFixedValueStatements(StringBuilder sb, String t, String bufVar, String indent, String scopeKey){
            if(t.equals("int") || t.equals("Integer") || t.equals("float") || t.equals("Float")){
                sb.append(indent).append(bufVar).append(".skip(4);\n");
                return;
            }
            if(t.equals("long") || t.equals("Long") || t.equals("double") || t.equals("Double")){
                sb.append(indent).append(bufVar).append(".skip(8);\n");
                return;
            }
            if(t.equals("short") || t.equals("Short") || t.equals("char") || t.equals("Character")){
                sb.append(indent).append(bufVar).append(".skip(2);\n");
                return;
            }
            if(t.equals("byte") || t.equals("Byte") || t.equals("boolean") || t.equals("Boolean")){
                sb.append(indent).append(bufVar).append(".skip(1);\n");
                return;
            }
            if(ENUMS.contains(t)){
                sb.append(indent).append(bufVar).append(".skip(4);\n");
                return;
            }
            if(t.endsWith("[]")){
                appendJavaSkipArrayValueStatements(sb, t, bufVar, indent, true, scopeKey);
                return;
            }
            if(isStructType(t)){
                sb.append(indent).append(t).append(".skip(").append(bufVar).append(");\n");
                return;
            }
            appendJavaSkipValueStatements(sb, t, bufVar, indent, scopeKey);
        }
        static void appendJavaSkipArrayValueStatements(StringBuilder sb, String t, String bufVar, String indent, boolean fixed, String scopeKey){
            String inner=t.substring(0, t.length()-2).trim();
            String countVar=childVar(scopeKey, "skipCount");
            String indexVar=childVar(scopeKey, "skipIndex");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            if(inner.equals("byte") || inner.equals("Byte") || inner.equals("boolean") || inner.equals("Boolean")){
                sb.append(indent).append("    if(").append(countVar).append("!=0) ").append(bufVar).append(".skip(").append(countVar).append(");\n");
            }else if(fixed && (inner.equals("int") || inner.equals("Integer") || inner.equals("float") || inner.equals("Float"))){
                sb.append(indent).append("    if(").append(countVar).append("!=0) ").append(bufVar).append(".skip(").append(countVar).append("*4);\n");
            }else if(fixed && (inner.equals("long") || inner.equals("Long") || inner.equals("double") || inner.equals("Double"))){
                sb.append(indent).append("    if(").append(countVar).append("!=0) ").append(bufVar).append(".skip(").append(countVar).append("*8);\n");
            }else if(fixed && (inner.equals("short") || inner.equals("Short") || inner.equals("char") || inner.equals("Character"))){
                sb.append(indent).append("    if(").append(countVar).append("!=0) ").append(bufVar).append(".skip(").append(countVar).append("*2);\n");
            }else if(!fixed && isJavaHotTailSkipStruct(inner)){
                sb.append(indent).append("    if(").append(countVar).append("!=0) ").append(inner).append(".skipMany(").append(bufVar).append(", ").append(countVar).append(");\n");
            }else{
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                if(fixed){
                    appendJavaSkipFixedValueStatements(sb, inner, bufVar, indent+"        ", scopeKey+"Elem");
                }else{
                    appendJavaSkipValueStatements(sb, inner, bufVar, indent+"        ", scopeKey+"Elem");
                }
                sb.append(indent).append("    }\n");
            }
            sb.append(indent).append("}\n");
        }
        static void appendJavaSkipCollectionValueStatements(StringBuilder sb, String inner, String bufVar, String indent, boolean fixed, String scopeKey){
            String countVar=childVar(scopeKey, "skipCount");
            String indexVar=childVar(scopeKey, "skipIndex");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            if(!fixed && isJavaHotTailSkipStruct(inner)){
                sb.append(indent).append("    if(").append(countVar).append("!=0) ").append(inner).append(".skipMany(").append(bufVar).append(", ").append(countVar).append(");\n");
            }else{
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                if(fixed){
                    appendJavaSkipFixedValueStatements(sb, inner, bufVar, indent+"        ", scopeKey+"Elem");
                }else{
                    appendJavaSkipValueStatements(sb, inner, bufVar, indent+"        ", scopeKey+"Elem");
                }
                sb.append(indent).append("    }\n");
            }
            sb.append(indent).append("}\n");
        }
        static void appendJavaSkipMapValueStatements(StringBuilder sb, String keyType, String valueType, String bufVar, String indent, boolean fixed, String scopeKey){
            String countVar=childVar(scopeKey, "skipCount");
            String indexVar=childVar(scopeKey, "skipIndex");
            sb.append(indent).append("{\n");
            sb.append(indent).append("    int ").append(countVar).append("=ByteIO.readSize(").append(bufVar).append(");\n");
            sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            if(fixed){
                appendJavaSkipFixedValueStatements(sb, keyType, bufVar, indent+"        ", scopeKey+"Key");
                appendJavaSkipFixedValueStatements(sb, valueType, bufVar, indent+"        ", scopeKey+"Value");
            }else{
                appendJavaSkipValueStatements(sb, keyType, bufVar, indent+"        ", scopeKey+"Key");
                appendJavaSkipValueStatements(sb, valueType, bufVar, indent+"        ", scopeKey+"Value");
            }
            sb.append(indent).append("    }\n");
            sb.append(indent).append("}\n");
        }
        static int javaRegularSkipScalarBytes(String t){
            if(t.equals("byte") || t.equals("Byte") || t.equals("boolean") || t.equals("Boolean")) return 1;
            if(t.equals("float") || t.equals("Float")) return 4;
            if(t.equals("double") || t.equals("Double")) return 8;
            return -1;
        }
        static int javaFixedSkipScalarBytes(String t){
            if(t.equals("int") || t.equals("Integer") || t.equals("float") || t.equals("Float")) return 4;
            if(t.equals("long") || t.equals("Long") || t.equals("double") || t.equals("Double")) return 8;
            if(t.equals("short") || t.equals("Short") || t.equals("char") || t.equals("Character")) return 2;
            if(t.equals("byte") || t.equals("Byte") || t.equals("boolean") || t.equals("Boolean")) return 1;
            if(ENUMS.contains(t)) return 4;
            return -1;
        }
        static void appendJavaFlushPendingSkipBytes(StringBuilder sb, String bufVar, String indent, int pendingBytes){
            if(pendingBytes>0){
                sb.append(indent).append(bufVar).append(".skip(").append(pendingBytes).append(");\n");
            }
        }
        static void appendJavaSkipFieldSequence(StringBuilder sb, List<Field> fields, String bufVar, String indent, boolean fixedLayout){
            int pendingBytes=0;
            for(Field f: fields){
                if(fieldHasMetadataDrivenCodec(f)){
                    appendJavaFlushPendingSkipBytes(sb, bufVar, indent, pendingBytes);
                    pendingBytes=0;
                    appendJavaSkipValueStatements(sb, f, bufVar, indent, f.name);
                    continue;
                }
                int scalarBytes=fixedLayout? javaFixedSkipScalarBytes(f.type) : javaRegularSkipScalarBytes(f.type);
                if(scalarBytes>0){
                    pendingBytes+=scalarBytes;
                    continue;
                }
                appendJavaFlushPendingSkipBytes(sb, bufVar, indent, pendingBytes);
                pendingBytes=0;
                if(fixedLayout){
                    appendJavaSkipFixedValueStatements(sb, f.type, bufVar, indent, f.name);
                }else{
                    appendJavaSkipValueStatements(sb, f.type, bufVar, indent, f.name);
                }
            }
            appendJavaFlushPendingSkipBytes(sb, bufVar, indent, pendingBytes);
        }
        static List<Field> dominantMaskSkipFields(Struct s, int presentTrackedCount){
            ArrayList<Field> fields=new ArrayList<>();
            int presenceIndex=0;
            for(Field f: s.fields){
                if(isPresenceTrackedType(f.type)){
                    if(presenceIndex++<presentTrackedCount){
                        fields.add(f);
                    }
                }else{
                    fields.add(f);
                }
            }
            return fields;
        }
        static String readValueCode(String bufVar, String t){
            return readValue(bufVar, t);
            /*
            if(t.equals("int")) return "BufUtil.readInt("+bufVar+")";
            if(t.equals("long")) return "BufUtil.readLong("+bufVar+")";
            if(t.equals("byte")) return "BufUtil.readByte("+bufVar+")";
            if(t.equals("short")) return "BufUtil.readShort("+bufVar+")";
            if(t.equals("boolean")) return "BufUtil.readBoolean("+bufVar+")";
            if(t.equals("char")) return "BufUtil.readChar("+bufVar+")";
            if(t.equals("float")) return "BufUtil.readFloat("+bufVar+")";
            if(t.equals("double")) return "BufUtil.readDouble("+bufVar+")";
            if(t.equals("Integer")) return "BufUtil.readInt("+bufVar+")";
            if(t.equals("Long")) return "BufUtil.readLong("+bufVar+")";
            if(t.equals("Byte")) return "BufUtil.readByte("+bufVar+")";
            if(t.equals("Short")) return "BufUtil.readShort("+bufVar+")";
            if(t.equals("Boolean")) return "BufUtil.readBoolean("+bufVar+")";
            if(t.equals("Character")) return "BufUtil.readChar("+bufVar+")";
            if(t.equals("Float")) return "BufUtil.readFloat("+bufVar+")";
            if(t.equals("Double")) return "BufUtil.readDouble("+bufVar+")";
            if(t.equals("String")) return "BufUtil.readString("+bufVar+")";
            if(t.startsWith("List<")){
                String inner=t.substring(5,t.length()-1);
                String elemBuf=childVar(bufVar, "elem");
                return "BufUtil.readList("+bufVar+", "+elemBuf+"->"+readValueCode(elemBuf,inner)+")";
            }
            if(t.startsWith("Set<")){
                String inner=t.substring(4,t.length()-1);
                String elemBuf=childVar(bufVar, "elem");
                return "BufUtil.readSet("+bufVar+", "+elemBuf+"->"+readValueCode(elemBuf,inner)+")";
            }
            if(t.startsWith("Map<")){
                String inside=t.substring(4,t.length()-1);
                java.util.List<String> kv=splitTopLevel(inside, ',');
                String kt=kv.get(0).trim(); String vt=kv.get(1).trim();
                String keyBuf=childVar(bufVar, "key");
                String valueBuf=childVar(bufVar, "value");
                return "BufUtil.readMap("+bufVar+", "+keyBuf+"->"+readValueCode(keyBuf,kt)+", "+valueBuf+"->"+readValueCode(valueBuf,vt)+")";
            }
            if(ENUMS.contains(t)) return t+".values()[BufUtil.readInt("+bufVar+")]";
            return t+".readFrom("+bufVar+")";
            */
        }
        static String writeValueCode(String bufVar, String var, String t){
            return writeValue(bufVar, var, t);
            /*
            if(t.equals("int")) return "BufUtil.writeInt("+bufVar+","+var+")";
            if(t.equals("long")) return "BufUtil.writeLong("+bufVar+","+var+")";
            if(t.equals("byte")) return "BufUtil.writeByte("+bufVar+","+var+")";
            if(t.equals("short")) return "BufUtil.writeShort("+bufVar+","+var+")";
            if(t.equals("boolean")) return "BufUtil.writeBoolean("+bufVar+","+var+")";
            if(t.equals("char")) return "BufUtil.writeChar("+bufVar+","+var+")";
            if(t.equals("float")) return "BufUtil.writeFloat("+bufVar+","+var+")";
            if(t.equals("double")) return "BufUtil.writeDouble("+bufVar+","+var+")";
            if(t.equals("Integer")) return "BufUtil.writeInt("+bufVar+","+var+")";
            if(t.equals("Long")) return "BufUtil.writeLong("+bufVar+","+var+")";
            if(t.equals("Byte")) return "BufUtil.writeByte("+bufVar+","+var+")";
            if(t.equals("Short")) return "BufUtil.writeShort("+bufVar+","+var+")";
            if(t.equals("Boolean")) return "BufUtil.writeBoolean("+bufVar+","+var+")";
            if(t.equals("Character")) return "BufUtil.writeChar("+bufVar+","+var+")";
            if(t.equals("Float")) return "BufUtil.writeFloat("+bufVar+","+var+")";
            if(t.equals("Double")) return "BufUtil.writeDouble("+bufVar+","+var+")";
            if(t.equals("String")) return "BufUtil.writeString("+bufVar+","+var+")";
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2);
                if(inner.equals("int")) return "BufUtil.writeIntArray("+bufVar+","+var+")";
                if(inner.equals("long")) return "BufUtil.writeLongArray("+bufVar+","+var+")";
                if(inner.equals("byte")) return "BufUtil.writeBytes("+bufVar+","+var+")";
                if(inner.equals("short")) return "BufUtil.writeShortArray("+bufVar+","+var+")";
                if(inner.equals("boolean")) return "BufUtil.writeBooleanArray("+bufVar+","+var+")";
                if(inner.equals("char")) return "BufUtil.writeCharArray("+bufVar+","+var+")";
                if(inner.equals("float")) return "BufUtil.writeFloatArray("+bufVar+","+var+")";
                if(inner.equals("double")) return "BufUtil.writeDoubleArray("+bufVar+","+var+")";
                String elemBuf=childVar(bufVar, "elem");
                String elemVar=childVar(var, "elem");
                return "BufUtil.writeObjectArray("+bufVar+","+var+", ("+elemBuf+","+elemVar+")->"+writeValue(elemBuf,elemVar,inner)+")";
            }
            if(t.startsWith("List<")){
                String inner=t.substring(5,t.length()-1);
                String elemBuf=childVar(bufVar, "elem");
                String elemVar=childVar(var, "elem");
                return "BufUtil.writeList("+bufVar+","+var+", ("+elemBuf+","+elemVar+")->"+writeValue(elemBuf,elemVar,inner)+")";
            }
            if(t.startsWith("Set<")){
                String inner=t.substring(4,t.length()-1);
                String elemBuf=childVar(bufVar, "elem");
                String elemVar=childVar(var, "elem");
                return "BufUtil.writeSet("+bufVar+","+var+", ("+elemBuf+","+elemVar+")->"+writeValue(elemBuf,elemVar,inner)+")";
            }
            if(t.startsWith("Map<")){
                String inside=t.substring(4,t.length()-1);
                java.util.List<String> kv=splitTopLevel(inside, ',');
                String kt=kv.get(0).trim(); String vt=kv.get(1).trim();
                String keyBuf=childVar(bufVar, "key");
                String valueBuf=childVar(bufVar, "value");
                String keyVar=childVar(var, "key");
                String valueVar=childVar(var, "value");
                return "BufUtil.writeMap("+bufVar+","+var+", ("+keyBuf+","+keyVar+")->"+writeValue(keyBuf,keyVar,kt)+", ("+valueBuf+","+valueVar+")->"+writeValue(valueBuf,valueVar,vt)+")";
            }
            if(ENUMS.contains(t)) return "BufUtil.writeInt("+bufVar+","+var+".ordinal())";
            return var+".writeTo("+bufVar+")";
            */
        }
        static String cap(String s){ return s.substring(0,1).toUpperCase()+s.substring(1); }
        static boolean isBorrowedBytesField(Field f){
            return f!=null && f.borrow && "byte[]".equals(f.type);
        }
        static boolean isBorrowedStringField(Field f){
            return f!=null && f.borrow && "String".equals(f.type);
        }
        static boolean isBorrowedPrimitiveArrayType(String t){
            return "int[]".equals(t) || "long[]".equals(t) || "float[]".equals(t) || "double[]".equals(t);
        }
        static boolean isBorrowedPrimitiveArrayField(Field f){
            return f!=null && f.borrow && isBorrowedPrimitiveArrayType(f.type);
        }
        static String borrowedArrayViewType(String t){
            if("int[]".equals(t)) return "IntArrayView";
            if("long[]".equals(t)) return "LongArrayView";
            if("float[]".equals(t)) return "FloatArrayView";
            if("double[]".equals(t)) return "DoubleArrayView";
            throw new IllegalArgumentException("unsupported borrowed primitive array type: "+t);
        }
        static String borrowedArrayEmptyExpr(String t){
            return borrowedArrayViewType(t)+".empty()";
        }
        static boolean isFixedLengthStringField(Field f){
            return f!=null && f.fixedLength!=null && "String".equals(f.type);
        }
        static boolean isFixedCountSupportedArrayType(String t){
            return "byte[]".equals(t)
                    || "int[]".equals(t)
                    || "long[]".equals(t)
                    || "float[]".equals(t)
                    || "double[]".equals(t);
        }
        static boolean isFixedCountArrayField(Field f){
            return f!=null && f.fixedLength!=null && isFixedCountSupportedArrayType(f.type);
        }
        static boolean isPackedFixedScalarType(String t){
            return javaFixedSkipScalarBytes(t)>0 || ENUMS.contains(t);
        }
        static boolean isPackedPrimitiveListField(Field f){
            return f!=null && f.packed && isListLikeType(f.type) && isPackedFixedScalarType(genericBody(f.type).trim());
        }
        static boolean isPackedPrimitiveMapField(Field f){
            if(f==null || !f.packed || !isMapLikeType(f.type)){
                return false;
            }
            List<String> kv=splitTopLevel(genericBody(f.type), ',');
            return kv.size()==2 && isPackedFixedScalarType(kv.get(0).trim()) && isPackedFixedScalarType(kv.get(1).trim());
        }
        static boolean isPackedIntKeyMapField(Field f){
            if(f==null || !f.packed || !isMapLikeType(f.type)){
                return false;
            }
            List<String> kv=splitTopLevel(genericBody(f.type), ',');
            return kv.size()==2 && isIntLikeType(kv.get(0).trim());
        }
        static boolean isPackedIntKeyObjectMapField(Field f){
            if(!isPackedIntKeyMapField(f)){
                return false;
            }
            List<String> kv=splitTopLevel(genericBody(f.type), ',');
            String valueType=kv.get(1).trim();
            return !isIntLikeType(valueType) && !isLongLikeType(valueType);
        }
        static boolean isPackedIntIntMapField(Field f){
            if(!isPackedPrimitiveMapField(f)){
                return false;
            }
            List<String> kv=splitTopLevel(genericBody(f.type), ',');
            return isIntLikeType(kv.get(0).trim()) && isIntLikeType(kv.get(1).trim());
        }
        static boolean isPackedIntLongMapField(Field f){
            if(!isPackedPrimitiveMapField(f)){
                return false;
            }
            List<String> kv=splitTopLevel(genericBody(f.type), ',');
            return isIntLikeType(kv.get(0).trim()) && isLongLikeType(kv.get(1).trim());
        }
        static boolean isBorrowProjectionField(Field f){
            return isBorrowedBytesField(f) || isBorrowedPrimitiveArrayField(f);
        }
        static boolean fieldHasMetadataDrivenJavaType(Field f){
            return isBorrowedBytesField(f) || isBorrowedStringField(f) || isBorrowedPrimitiveArrayField(f);
        }
        static boolean fieldHasMetadataDrivenCodec(Field f){
            return fieldHasMetadataDrivenJavaType(f)
                    || isFixedLengthStringField(f)
                    || isFixedCountArrayField(f)
                    || isPackedPrimitiveListField(f)
                    || isPackedPrimitiveMapField(f)
                    || isPackedIntKeyObjectMapField(f);
        }
        static String mapType(Field f){
            if(isBorrowedBytesField(f)){
                return "BorrowedBytes";
            }
            if(isBorrowedStringField(f)){
                return "BorrowedString";
            }
            if(isBorrowedPrimitiveArrayField(f)){
                return borrowedArrayViewType(f.type);
            }
            return mapType(f.type);
        }
        static String mapType(String t){
            if(isPrimitive(t) || t.equals("String")) return t;
            if(t.endsWith("[]")) return mapType(t.substring(0,t.length()-2))+"[]";
            if(isContainerType(t)) return toGenericWithWrappers(t);
            return SiCompiler.toCamel(t); // struct or enum
        }
        static boolean isContainerType(String t){
            return isOptionalType(t) || isListLikeType(t) || isSetLikeType(t) || isMapLikeType(t) || isQueueLikeType(t);
        }
        static boolean isProjectionLimitedType(String t){
            return t.endsWith("[]") || isListLikeType(t) || isSetLikeType(t) || isMapLikeType(t) || isQueueLikeType(t);
        }
        static boolean isProjectionIndexedType(String t){
            return "byte[]".equals(t) || "Byte[]".equals(t);
        }
        static boolean isOptionalType(String t){
            return startsIgnoreCase(t, "optional<");
        }
        static boolean isListLikeType(String t){
            return startsWithAnyIgnoreCase(t, "list<", "arraylist<", "linkedlist<", "collection<");
        }
        static boolean isSetLikeType(String t){
            return startsWithAnyIgnoreCase(t, "set<", "hashset<", "linkedhashset<");
        }
        static boolean isMapLikeType(String t){
            return startsWithAnyIgnoreCase(t, "map<", "hashmap<", "linkedhashmap<");
        }
        static boolean isQueueLikeType(String t){
            return startsWithAnyIgnoreCase(t, "queue<", "deque<");
        }
        static String rawGenericType(String t){
            int idx=t.indexOf('<');
            return idx<0? t: t.substring(0, idx);
        }
        static String genericBody(String t){
            int idx=t.indexOf('<');
            return idx<0? "": t.substring(idx+1, t.length()-1);
        }
        static String javaArrayAllocationExpr(String arrayType, String sizeExpr){
            String componentType=arrayType.substring(0, arrayType.length()-2).trim();
            if(isContainerType(componentType)){
                return "("+mapType(arrayType)+") new "+canonicalContainerType(componentType)+"["+sizeExpr+"]";
            }
            return "new "+mapType(componentType)+"["+sizeExpr+"]";
        }
        static String javaObjectArrayCreatorExpr(String arrayType, String sizeVar){
            return sizeVar+"->"+javaArrayAllocationExpr(arrayType, sizeVar);
        }
        static String canonicalContainerType(String t){
            String raw=rawGenericType(t).toLowerCase(Locale.ROOT);
            switch (raw){
                case "list": return "List";
                case "arraylist": return "ArrayList";
                case "linkedlist": return "LinkedList";
                case "collection": return "Collection";
                case "set": return "Set";
                case "hashset": return "HashSet";
                case "linkedhashset": return "LinkedHashSet";
                case "map": return "Map";
                case "hashmap": return "HashMap";
                case "linkedhashmap": return "LinkedHashMap";
                case "queue": return "Queue";
                case "deque": return "Deque";
                case "optional": return "Optional";
                default: return rawGenericType(t);
            }
        }
        static String toGenericWithWrappers(String t){
            if(isOptionalType(t)){
                return "Optional<"+wrapGeneric(genericBody(t).trim())+">";
            }
            if(isListLikeType(t) || isSetLikeType(t) || isQueueLikeType(t)){
                return canonicalContainerType(t)+"<"+wrapGeneric(genericBody(t).trim())+">";
            }
            if(isMapLikeType(t)){
                java.util.List<String> kv=splitTopLevel(genericBody(t), ',');
                String kt=wrapGeneric(kv.get(0).trim());
                String vt=wrapGeneric(kv.get(1).trim());
                return canonicalContainerType(t)+"<"+kt+","+vt+">";
            }
            return t;
        }
        static String wrapGeneric(String x){
            if(x.endsWith("[]")) return mapType(x.substring(0,x.length()-2))+"[]";
            if(isPrimitive(x)){
                switch (x){
                    case "int": return "Integer";
                    case "long": return "Long";
                    case "byte": return "Byte";
                    case "short": return "Short";
                    case "boolean": return "Boolean";
                    case "char": return "Character";
                    case "float": return "Float";
                    case "double": return "Double";
                }
            }
            if(isContainerType(x)) return toGenericWithWrappers(x);
            return SiCompiler.toCamel(x);
        }
        static java.util.List<String> splitTopLevel(String s, char sep){
            java.util.List<String> out=new java.util.ArrayList<>();
            int lvl=0; int last=0;
            for(int i=0;i<s.length();i++){
                char c=s.charAt(i);
                if(c=='<') lvl++;
                else if(c=='>') lvl--;
                else if(c==sep && lvl==0){
                    out.add(s.substring(last,i));
                    last=i+1;
                }
            }
            out.add(s.substring(last));
            return out;
        }
        static boolean startsIgnoreCase(String s, String p){
            return s.regionMatches(true,0,p,0,p.length());
        }
        static boolean startsWithAnyIgnoreCase(String s, String... prefixes){
            for(String prefix: prefixes){
                if(startsIgnoreCase(s, prefix)) return true;
            }
            return false;
        }
        static boolean isPrimitive(String t){
            return t.equals("int")||t.equals("long")||t.equals("byte")||t.equals("short")
                    ||t.equals("boolean")||t.equals("char")||t.equals("float")||t.equals("double");
        }
        static String readExpr(String t){
            return readValue("buf", t);
            /*
            if(t.equals("int")) return "BufUtil.readInt(buf)";
            if(t.equals("long")) return "BufUtil.readLong(buf)";
            if(t.equals("byte")) return "BufUtil.readByte(buf)";
            if(t.equals("short")) return "BufUtil.readShort(buf)";
            if(t.equals("boolean")) return "BufUtil.readBoolean(buf)";
            if(t.equals("char")) return "BufUtil.readChar(buf)";
            if(t.equals("float")) return "BufUtil.readFloat(buf)";
            if(t.equals("double")) return "BufUtil.readDouble(buf)";
            if(t.equals("Integer")) return "BufUtil.readInt(buf)";
            if(t.equals("Long")) return "BufUtil.readLong(buf)";
            if(t.equals("Byte")) return "BufUtil.readByte(buf)";
            if(t.equals("Short")) return "BufUtil.readShort(buf)";
            if(t.equals("Boolean")) return "BufUtil.readBoolean(buf)";
            if(t.equals("Character")) return "BufUtil.readChar(buf)";
            if(t.equals("Float")) return "BufUtil.readFloat(buf)";
            if(t.equals("Double")) return "BufUtil.readDouble(buf)";
            if(t.equals("String")) return "BufUtil.readString(buf)";
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2);
                if(inner.equals("int")) return "BufUtil.readIntArray(buf)";
                if(inner.equals("long")) return "BufUtil.readLongArray(buf)";
                if(inner.equals("byte")) return "BufUtil.readBytes(buf)";
                if(inner.equals("short")) return "BufUtil.readShortArray(buf)";
                if(inner.equals("boolean")) return "BufUtil.readBooleanArray(buf)";
                if(inner.equals("char")) return "BufUtil.readCharArray(buf)";
                if(inner.equals("float")) return "BufUtil.readFloatArray(buf)";
                if(inner.equals("double")) return "BufUtil.readDoubleArray(buf)";
                return "BufUtil.readObjectArray(buf, "+inner+"[]::new, b->"+readValue(inner)+")";
            }
            if(t.startsWith("List<")){
                String inner=t.substring(5,t.length()-1);
                return "BufUtil.readList(buf, b->"+readValue(inner)+")";
            }
            if(t.startsWith("Set<")){
                String inner=t.substring(4,t.length()-1);
                return "BufUtil.readSet(buf, b->"+readValue(inner)+")";
            }
            if(t.startsWith("Map<")){
                String inside=t.substring(4,t.length()-1);
                java.util.List<String> kv=splitTopLevel(inside, ',');
                if(kv.size()!=2) throw new IllegalArgumentException("Map 婵犵數濮烽弫鍛婃叏閻戣棄鏋侀柛娑橈攻閸欏繘鏌ｉ幋锝嗩棄闁哄绶氶弻娑樷槈濮楀牊鏁鹃梺鍛婄懃缁绘﹢寮婚敐澶婄闁挎繂妫Λ鍕⒑閸濆嫷鍎庣紒鑸靛哺瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁诡垎鍐ｆ寖缂備緡鍣崹鎶藉箲閵忕姭妲堥柕蹇曞Х椤撴椽姊洪崫鍕殜闁稿鎹囬弻娑㈠Χ閸涱垍褔鏌＄仦鍓ф创濠碉紕鍏橀、娆撴偂鎼搭喗浜ょ紓鍌氬€烽懗鑸垫叏娴兼潙纾块柡灞诲劚妗呴梺鍛婃处閸ㄦ澘螞濮椻偓閺屾盯鈥﹂幋婵囩亐闂佸湱顭堥崯鏉戭潖閾忓湱纾兼俊顖濐嚙闂夊秹姊洪崷顓熷殌婵炲眰鍊濋敐鐐剁疀濞戞瑦鍎柣鐔哥懃鐎氼剟宕㈣ぐ鎺撯拺闁告稑锕ｇ欢閬嶆煕閻樺磭澧柍钘夘槼椤﹁鎱ㄦ繝鍌ょ吋鐎规洘甯掗～婵嬵敆婢跺鍋呴梻浣筋嚙鐎涒晝鍠婂鍜佺唵婵せ鍋撻柟顕€绠栭幃婊堟寠婢跺瞼鏉搁梻浣虹帛椤ㄥ懘鎮ч崱妯碱浄婵炲樊浜濋埛鎴︽煕濞戞﹫鍔熼柍钘夘樀閺屻劑寮撮鍡欐殼閻庤娲忛崹铏圭矉閹烘柡鍋撻敐搴′簮闁圭柉娅ｇ槐鎺旂磼閵忕姴绠归梺鐟板暱缁绘濡甸幇顖ｆЬ缂備浇椴哥敮鈥愁嚕婵犳艾唯鐟滃繘寮抽锔界參婵☆垳鍘ч弸搴亜椤撶偞鍋ラ柟铏矒濡啫鈽夊鍡樼秾闂傚倷鑳剁划顖炲箰妤ｅ啫绐楅柟鐗堟緲妗呴梺鍛婃处閸ㄦ壆绮绘繝姘仯闁惧繒鎳撻崝瀣煟濠靛啠鍋撻弬銉︽杸闂佺粯鍔樼亸娆愭櫠濞戞氨纾兼い鏃囧Г瀹曞瞼鈧鍠栭…鐑藉极瀹ュ绀嬫い蹇撴噹婵即姊绘担鍛婂暈闁圭妫欓幏鍛村箵閹哄秴顥氶梻浣告贡閸庛倝宕靛鐐戒汗闁圭儤鎸告禍濂告⒑閸涘﹣绶遍柛銊﹀▕瀵娊濡舵径瀣偓鐢告偡濞嗗繐顏紒鈧崘顏嗙＜妞ゆ棁鍋愮粻濠氭寠濠靛洢浜滈柡鍌濇硶濮ｇ偤鏌￠埀顒勬嚍閵夛絼绨婚梺鍝勫€搁悘婵嬪箖閹达附鐓欓柛蹇曞帶婵秹鏌＄仦鐣屝ら柟鍙夋尦瀹曠喖顢曢妶鍕闂傚倷娴囬鏍窗濡ゅ懏鍋￠柍鍝勬噹缁犵娀鏌ｅΟ缁樸仧闁轰礁妫濋弻宥堫檨闁告挾鍠栧畷娲川閺夋垹顔岄梺鐟版惈濡瑩寮埀顒勬⒒娴ｈ櫣甯涢柛鏃€娲栬灒濠电姴浼ｉ敐鍫涗汗闁圭儤鎸鹃崢浠嬫⒑閸愬弶鎯堥柨鏇樺€濋幃姗€鏁傞柨顖氫壕閻熸瑥瀚粈鍐╀繆閻愯泛袚濞ｅ洤锕幃婊堟嚍閵夛附鐝栭梻渚€鈧偛鑻晶鐗堢箾閸℃劕鐏查柟顔界懇閹粌螣閻撳骸绠ラ梻鍌氬€风欢锟犲矗韫囨洜涓嶉柟瀵稿仦濞呯娀鏌ｅΔ鈧悧鍕濠婂牊鐓涢柛鎰剁到娴滈箖鏌ｉ姀鈺佺伈缂佺粯绻堥悰顕€宕橀妸搴㈡瀹曟﹢鍩℃担鍦偓顓㈡⒒娴ｅ憡鍟炴繛璇х畵瀹曟粌鈽夐埗鍝勬喘婵＄兘濡烽姀鈩冩澑闂備胶绮崝鏇炍熸繝鍌ょ€剁憸蹇曟閹烘鍋愰柧蹇ｅ亜闂夊秹姊洪柅娑氣敀闁告梹鍨垮畷娲焵椤掍降浜滈柟鐑樺灥椤忣亪鏌ｉ幘宕囩闁哄本鐩、鏇㈡晲閸モ晝鏆俊鐐€戦崕铏箾閳ь剟鏌＄仦鍓р槈妞ゎ偅绮撻崺鈧い鎺嗗亾妞ゎ厼娲╅ˇ褰掓煃閵夛附顥堢€规洘锕㈤、娆撳床婢诡垰娲﹂悡鏇㈡煃閳轰礁鏋ら柣鎺嶇矙閺屾稑鈻庤箛鏃戞＆濠殿喖锕ュ钘壩涢崘銊㈡婵﹩鍓﹂弶鎼佹⒒娴ｇ瓔鍤冮柛鐘崇墵瀹曟劙宕稿Δ鈧繚婵炴挻鍩冮崑鎾搭殽閻愮柕顏堚€﹂崸妤€绀嬫い鎺嶇贰濡啴鎮楃憴鍕８闁告梹鍨块妴浣糕枎閹惧磭鐣鹃悷婊冪Ф缁? "+t);
                String kt=kv.get(0).trim(); String vt=kv.get(1).trim();
                return "BufUtil.readMap(buf, b->"+readValue(kt)+", b->"+readValue(vt)+")";
            }
            if(ENUMS.contains(t)) return t+".values()[BufUtil.readInt(buf)]";
            return t+".readFrom(buf)";
            */
        }
        static String writeStmt(String var,String t){
            return writeValue("buf", var, t)+";";
            /*
            if(t.equals("int")) return "BufUtil.writeInt(buf,"+var+");";
            if(t.equals("long")) return "BufUtil.writeLong(buf,"+var+");";
            if(t.equals("byte")) return "BufUtil.writeByte(buf,"+var+");";
            if(t.equals("short")) return "BufUtil.writeShort(buf,"+var+");";
            if(t.equals("boolean")) return "BufUtil.writeBoolean(buf,"+var+");";
            if(t.equals("char")) return "BufUtil.writeChar(buf,"+var+");";
            if(t.equals("float")) return "BufUtil.writeFloat(buf,"+var+");";
            if(t.equals("double")) return "BufUtil.writeDouble(buf,"+var+");";
            if(t.equals("Integer")) return "BufUtil.writeInt(buf,"+var+");";
            if(t.equals("Long")) return "BufUtil.writeLong(buf,"+var+");";
            if(t.equals("Byte")) return "BufUtil.writeByte(buf,"+var+");";
            if(t.equals("Short")) return "BufUtil.writeShort(buf,"+var+");";
            if(t.equals("Boolean")) return "BufUtil.writeBoolean(buf,"+var+");";
            if(t.equals("Character")) return "BufUtil.writeChar(buf,"+var+");";
            if(t.equals("Float")) return "BufUtil.writeFloat(buf,"+var+");";
            if(t.equals("Double")) return "BufUtil.writeDouble(buf,"+var+");";
            if(t.equals("String")) return "BufUtil.writeString(buf,"+var+");";
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2);
                if(inner.equals("int")) return "BufUtil.writeIntArray(buf,"+var+");";
                if(inner.equals("long")) return "BufUtil.writeLongArray(buf,"+var+");";
                if(inner.equals("byte")) return "BufUtil.writeBytes(buf,"+var+");";
                if(inner.equals("short")) return "BufUtil.writeShortArray(buf,"+var+");";
                if(inner.equals("boolean")) return "BufUtil.writeBooleanArray(buf,"+var+");";
                if(inner.equals("char")) return "BufUtil.writeCharArray(buf,"+var+");";
                if(inner.equals("float")) return "BufUtil.writeFloatArray(buf,"+var+");";
                if(inner.equals("double")) return "BufUtil.writeDoubleArray(buf,"+var+");";
                return "BufUtil.writeObjectArray(buf,"+var+", (b,v)->"+writeValue("v",inner)+");";
            }
            if(t.startsWith("List<")){
                String inner=t.substring(5,t.length()-1);
                return "BufUtil.writeList(buf,"+var+", (b,v)->"+writeValue("v",inner)+");";
            }
            if(t.startsWith("Set<")){
                String inner=t.substring(4,t.length()-1);
                return "BufUtil.writeSet(buf,"+var+", (b,v)->"+writeValue("v",inner)+");";
            }
            if(t.startsWith("Map<")){
                String inside=t.substring(4,t.length()-1);
                java.util.List<String> kv=splitTopLevel(inside, ',');
                if(kv.size()!=2) throw new IllegalArgumentException("Map 婵犵數濮烽弫鍛婃叏閻戣棄鏋侀柛娑橈攻閸欏繘鏌ｉ幋锝嗩棄闁哄绶氶弻娑樷槈濮楀牊鏁鹃梺鍛婄懃缁绘﹢寮婚敐澶婄闁挎繂妫Λ鍕⒑閸濆嫷鍎庣紒鑸靛哺瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁诡垎鍐ｆ寖缂備緡鍣崹鎶藉箲閵忕姭妲堥柕蹇曞Х椤撴椽姊洪崫鍕殜闁稿鎹囬弻娑㈠Χ閸涱垍褔鏌＄仦鍓ф创濠碉紕鍏橀、娆撴偂鎼搭喗浜ょ紓鍌氬€烽懗鑸垫叏娴兼潙纾块柡灞诲劚妗呴梺鍛婃处閸ㄦ澘螞濮椻偓閺屾盯鈥﹂幋婵囩亐闂佸湱顭堥崯鏉戭潖閾忓湱纾兼俊顖濐嚙闂夊秹姊洪崷顓熷殌婵炲眰鍊濋敐鐐剁疀濞戞瑦鍎柣鐔哥懃鐎氼剟宕㈣ぐ鎺撯拺闁告稑锕ｇ欢閬嶆煕閻樺磭澧柍钘夘槼椤﹁鎱ㄦ繝鍌ょ吋鐎规洘甯掗～婵嬵敆婢跺鍋呴梻浣筋嚙鐎涒晝鍠婂鍜佺唵婵せ鍋撻柟顕€绠栭幃婊堟寠婢跺瞼鏉搁梻浣虹帛椤ㄥ懘鎮ч崱妯碱浄婵炲樊浜濋埛鎴︽煕濞戞﹫鍔熼柍钘夘樀閺屻劑寮撮鍡欐殼閻庤娲忛崹铏圭矉閹烘柡鍋撻敐搴′簮闁圭柉娅ｇ槐鎺旂磼閵忕姴绠归梺鐟板暱缁绘濡甸幇顖ｆЬ缂備浇椴哥敮鈥愁嚕婵犳艾唯鐟滃繘寮抽锔界參婵☆垳鍘ч弸搴亜椤撶偞鍋ラ柟铏矒濡啫鈽夊鍡樼秾闂傚倷鑳剁划顖炲箰妤ｅ啫绐楅柟鐗堟緲妗呴梺鍛婃处閸ㄦ壆绮绘繝姘仯闁惧繒鎳撻崝瀣煟濠靛啠鍋撻弬銉︽杸闂佺粯鍔樼亸娆愭櫠濞戞氨纾兼い鏃囧Г瀹曞瞼鈧鍠栭…鐑藉极瀹ュ绀嬫い蹇撴噹婵即姊绘担鍛婂暈闁圭妫欓幏鍛村箵閹哄秴顥氶梻浣告贡閸庛倝宕靛鐐戒汗闁圭儤鎸告禍濂告⒑閸涘﹣绶遍柛銊﹀▕瀵娊濡舵径瀣偓鐢告偡濞嗗繐顏紒鈧崘顏嗙＜妞ゆ棁鍋愮粻濠氭寠濠靛洢浜滈柡鍌濇硶濮ｇ偤鏌￠埀顒勬嚍閵夛絼绨婚梺鍝勫€搁悘婵嬪箖閹达附鐓欓柛蹇曞帶婵秹鏌＄仦鐣屝ら柟鍙夋尦瀹曠喖顢曢妶鍕闂傚倷娴囬鏍窗濡ゅ懏鍋￠柍鍝勬噹缁犵娀鏌ｅΟ缁樸仧闁轰礁妫濋弻宥堫檨闁告挾鍠栧畷娲川閺夋垹顔岄梺鐟版惈濡瑩寮埀顒勬⒒娴ｈ櫣甯涢柛鏃€娲栬灒濠电姴浼ｉ敐鍫涗汗闁圭儤鎸鹃崢浠嬫⒑閸愬弶鎯堥柨鏇樺€濋幃姗€鏁傞柨顖氫壕閻熸瑥瀚粈鍐╀繆閻愯泛袚濞ｅ洤锕幃婊堟嚍閵夛附鐝栭梻渚€鈧偛鑻晶鐗堢箾閸℃劕鐏查柟顔界懇閹粌螣閻撳骸绠ラ梻鍌氬€风欢锟犲矗韫囨洜涓嶉柟瀵稿仦濞呯娀鏌ｅΔ鈧悧鍕濠婂牊鐓涢柛鎰剁到娴滈箖鏌ｉ姀鈺佺伈缂佺粯绻堥悰顕€宕橀妸搴㈡瀹曟﹢鍩℃担鍦偓顓㈡⒒娴ｅ憡鍟炴繛璇х畵瀹曟粌鈽夐埗鍝勬喘婵＄兘濡烽姀鈩冩澑闂備胶绮崝鏇炍熸繝鍌ょ€剁憸蹇曟閹烘鍋愰柧蹇ｅ亜闂夊秹姊洪柅娑氣敀闁告梹鍨垮畷娲焵椤掍降浜滈柟鐑樺灥椤忣亪鏌ｉ幘宕囩闁哄本鐩、鏇㈡晲閸モ晝鏆俊鐐€戦崕铏箾閳ь剟鏌＄仦鍓р槈妞ゎ偅绮撻崺鈧い鎺嗗亾妞ゎ厼娲╅ˇ褰掓煃閵夛附顥堢€规洘锕㈤、娆撳床婢诡垰娲﹂悡鏇㈡煃閳轰礁鏋ら柣鎺嶇矙閺屾稑鈻庤箛鏃戞＆濠殿喖锕ュ钘壩涢崘銊㈡婵﹩鍓﹂弶鎼佹⒒娴ｇ瓔鍤冮柛鐘崇墵瀹曟劙宕稿Δ鈧繚婵炴挻鍩冮崑鎾搭殽閻愮柕顏堚€﹂崸妤€绀嬫い鎺嶇贰濡啴鎮楃憴鍕８闁告梹鍨块妴浣糕枎閹惧磭鐣鹃悷婊冪Ф缁? "+t);
                String kt=kv.get(0).trim(); String vt=kv.get(1).trim();
                return "BufUtil.writeMap(buf,"+var+", (b,k)->"+writeValue("k",kt)+", (b,v)->"+writeValue("v",vt)+");";
            }
            if(ENUMS.contains(t)) return "BufUtil.writeInt(buf,"+var+".ordinal());";
            return var+".writeTo(buf);";
            */
        }
        static String readValue(String t){
            return readValue("b", t);
            /*
            if(t.equals("int")) return "BufUtil.readInt(b)";
            if(t.equals("long")) return "BufUtil.readLong(b)";
            if(t.equals("byte")) return "BufUtil.readByte(b)";
            if(t.equals("short")) return "BufUtil.readShort(b)";
            if(t.equals("boolean")) return "BufUtil.readBoolean(b)";
            if(t.equals("char")) return "BufUtil.readChar(b)";
            if(t.equals("float")) return "BufUtil.readFloat(b)";
            if(t.equals("double")) return "BufUtil.readDouble(b)";
            if(t.equals("Integer")) return "BufUtil.readInt(b)";
            if(t.equals("Long")) return "BufUtil.readLong(b)";
            if(t.equals("Byte")) return "BufUtil.readByte(b)";
            if(t.equals("Short")) return "BufUtil.readShort(b)";
            if(t.equals("Boolean")) return "BufUtil.readBoolean(b)";
            if(t.equals("Character")) return "BufUtil.readChar(b)";
            if(t.equals("Float")) return "BufUtil.readFloat(b)";
            if(t.equals("Double")) return "BufUtil.readDouble(b)";
            if(t.equals("String")) return "BufUtil.readString(b)";
            if(t.startsWith("List<")){
                String inner=t.substring(5,t.length()-1);
                return "BufUtil.readList(b, x->"+readValue(inner)+")";
            }
            if(t.startsWith("Set<")){
                String inner=t.substring(4,t.length()-1);
                return "BufUtil.readSet(b, x->"+readValue(inner)+")";
            }
            if(t.startsWith("Map<")){
                String inside=t.substring(4,t.length()-1);
                java.util.List<String> kv=splitTopLevel(inside, ',');
                if(kv.size()!=2) throw new IllegalArgumentException("Map 婵犵數濮烽弫鍛婃叏閻戣棄鏋侀柛娑橈攻閸欏繘鏌ｉ幋锝嗩棄闁哄绶氶弻娑樷槈濮楀牊鏁鹃梺鍛婄懃缁绘﹢寮婚敐澶婄闁挎繂妫Λ鍕⒑閸濆嫷鍎庣紒鑸靛哺瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁诡垎鍐ｆ寖缂備緡鍣崹鎶藉箲閵忕姭妲堥柕蹇曞Х椤撴椽姊洪崫鍕殜闁稿鎹囬弻娑㈠Χ閸涱垍褔鏌＄仦鍓ф创濠碉紕鍏橀、娆撴偂鎼搭喗浜ょ紓鍌氬€烽懗鑸垫叏娴兼潙纾块柡灞诲劚妗呴梺鍛婃处閸ㄦ澘螞濮椻偓閺屾盯鈥﹂幋婵囩亐闂佸湱顭堥崯鏉戭潖閾忓湱纾兼俊顖濐嚙闂夊秹姊洪崷顓熷殌婵炲眰鍊濋敐鐐剁疀濞戞瑦鍎柣鐔哥懃鐎氼剟宕㈣ぐ鎺撯拺闁告稑锕ｇ欢閬嶆煕閻樺磭澧柍钘夘槼椤﹁鎱ㄦ繝鍌ょ吋鐎规洘甯掗～婵嬵敆婢跺鍋呴梻浣筋嚙鐎涒晝鍠婂鍜佺唵婵せ鍋撻柟顕€绠栭幃婊堟寠婢跺瞼鏉搁梻浣虹帛椤ㄥ懘鎮ч崱妯碱浄婵炲樊浜濋埛鎴︽煕濞戞﹫鍔熼柍钘夘樀閺屻劑寮撮鍡欐殼閻庤娲忛崹铏圭矉閹烘柡鍋撻敐搴′簮闁圭柉娅ｇ槐鎺旂磼閵忕姴绠归梺鐟板暱缁绘濡甸幇顖ｆЬ缂備浇椴哥敮鈥愁嚕婵犳艾唯鐟滃繘寮抽锔界參婵☆垳鍘ч弸搴亜椤撶偞鍋ラ柟铏矒濡啫鈽夊鍡樼秾闂傚倷鑳剁划顖炲箰妤ｅ啫绐楅柟鐗堟緲妗呴梺鍛婃处閸ㄦ壆绮绘繝姘仯闁惧繒鎳撻崝瀣煟濠靛啠鍋撻弬銉︽杸闂佺粯鍔樼亸娆愭櫠濞戞氨纾兼い鏃囧Г瀹曞瞼鈧鍠栭…鐑藉极瀹ュ绀嬫い蹇撴噹婵即姊绘担鍛婂暈闁圭妫欓幏鍛村箵閹哄秴顥氶梻浣告贡閸庛倝宕靛鐐戒汗闁圭儤鎸告禍濂告⒑閸涘﹣绶遍柛銊﹀▕瀵娊濡舵径瀣偓鐢告偡濞嗗繐顏紒鈧崘顏嗙＜妞ゆ棁鍋愮粻濠氭寠濠靛洢浜滈柡鍌濇硶濮ｇ偤鏌￠埀顒勬嚍閵夛絼绨婚梺鍝勫€搁悘婵嬪箖閹达附鐓欓柛蹇曞帶婵秹鏌＄仦鐣屝ら柟鍙夋尦瀹曠喖顢曢妶鍕闂傚倷娴囬鏍窗濡ゅ懏鍋￠柍鍝勬噹缁犵娀鏌ｅΟ缁樸仧闁轰礁妫濋弻宥堫檨闁告挾鍠栧畷娲川閺夋垹顔岄梺鐟版惈濡瑩寮埀顒勬⒒娴ｈ櫣甯涢柛鏃€娲栬灒濠电姴浼ｉ敐鍫涗汗闁圭儤鎸鹃崢浠嬫⒑閸愬弶鎯堥柨鏇樺€濋幃姗€鏁傞柨顖氫壕閻熸瑥瀚粈鍐╀繆閻愯泛袚濞ｅ洤锕幃婊堟嚍閵夛附鐝栭梻渚€鈧偛鑻晶鐗堢箾閸℃劕鐏查柟顔界懇閹粌螣閻撳骸绠ラ梻鍌氬€风欢锟犲矗韫囨洜涓嶉柟瀵稿仦濞呯娀鏌ｅΔ鈧悧鍕濠婂牊鐓涢柛鎰剁到娴滈箖鏌ｉ姀鈺佺伈缂佺粯绻堥悰顕€宕橀妸搴㈡瀹曟﹢鍩℃担鍦偓顓㈡⒒娴ｅ憡鍟炴繛璇х畵瀹曟粌鈽夐埗鍝勬喘婵＄兘濡烽姀鈩冩澑闂備胶绮崝鏇炍熸繝鍌ょ€剁憸蹇曟閹烘鍋愰柧蹇ｅ亜闂夊秹姊洪柅娑氣敀闁告梹鍨垮畷娲焵椤掍降浜滈柟鐑樺灥椤忣亪鏌ｉ幘宕囩闁哄本鐩、鏇㈡晲閸モ晝鏆俊鐐€戦崕铏箾閳ь剟鏌＄仦鍓р槈妞ゎ偅绮撻崺鈧い鎺嗗亾妞ゎ厼娲╅ˇ褰掓煃閵夛附顥堢€规洘锕㈤、娆撳床婢诡垰娲﹂悡鏇㈡煃閳轰礁鏋ら柣鎺嶇矙閺屾稑鈻庤箛鏃戞＆濠殿喖锕ュ钘壩涢崘銊㈡婵﹩鍓﹂弶鎼佹⒒娴ｇ瓔鍤冮柛鐘崇墵瀹曟劙宕稿Δ鈧繚婵炴挻鍩冮崑鎾搭殽閻愮柕顏堚€﹂崸妤€绀嬫い鎺嶇贰濡啴鎮楃憴鍕８闁告梹鍨块妴浣糕枎閹惧磭鐣鹃悷婊冪Ф缁? "+t);
                String kt=kv.get(0).trim(); String vt=kv.get(1).trim();
                return "BufUtil.readMap(b, x->"+readValue(kt)+", x->"+readValue(vt)+")";
            }
            if(ENUMS.contains(t)) return t+".values()[BufUtil.readInt(b)]";
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2);
                if(inner.equals("int")) return "BufUtil.readIntArray(b)";
                if(inner.equals("long")) return "BufUtil.readLongArray(b)";
                if(inner.equals("byte")) return "BufUtil.readBytes(b)";
                if(inner.equals("short")) return "BufUtil.readShortArray(b)";
                if(inner.equals("boolean")) return "BufUtil.readBooleanArray(b)";
                if(inner.equals("char")) return "BufUtil.readCharArray(b)";
                if(inner.equals("float")) return "BufUtil.readFloatArray(b)";
                if(inner.equals("double")) return "BufUtil.readDoubleArray(b)";
                return "BufUtil.readObjectArray(b, "+inner+"[]::new, x->"+readValue(inner)+")";
            }
            return t+".readFrom(b)";
            */
        }
        static String readValue(String bufVar, String t){
            if(t.equals("int")) return "BufUtil.readInt("+bufVar+")";
            if(t.equals("long")) return "BufUtil.readLong("+bufVar+")";
            if(t.equals("byte")) return "BufUtil.readByte("+bufVar+")";
            if(t.equals("short")) return "BufUtil.readShort("+bufVar+")";
            if(t.equals("boolean")) return "BufUtil.readBoolean("+bufVar+")";
            if(t.equals("char")) return "BufUtil.readChar("+bufVar+")";
            if(t.equals("float")) return "BufUtil.readFloat("+bufVar+")";
            if(t.equals("double")) return "BufUtil.readDouble("+bufVar+")";
            if(t.equals("Integer")) return "BufUtil.readInt("+bufVar+")";
            if(t.equals("Long")) return "BufUtil.readLong("+bufVar+")";
            if(t.equals("Byte")) return "BufUtil.readByte("+bufVar+")";
            if(t.equals("Short")) return "BufUtil.readShort("+bufVar+")";
            if(t.equals("Boolean")) return "BufUtil.readBoolean("+bufVar+")";
            if(t.equals("Character")) return "BufUtil.readChar("+bufVar+")";
            if(t.equals("Float")) return "BufUtil.readFloat("+bufVar+")";
            if(t.equals("Double")) return "BufUtil.readDouble("+bufVar+")";
            if(t.equals("String")) return "BufUtil.readString("+bufVar+")";
            if(ENUMS.contains(t)) return t+".fromOrdinal(BufUtil.readUInt("+bufVar+"))";
            if(isOptionalType(t)){
                String inner=genericBody(t).trim();
                String optBuf=childVar(bufVar, "optional");
                return "BufUtil.readOptional("+bufVar+", "+optBuf+"->"+readValue(optBuf, inner)+")";
            }
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2);
                if(inner.equals("int")) return "BufUtil.readIntArray("+bufVar+")";
                if(inner.equals("long")) return "BufUtil.readLongArray("+bufVar+")";
                if(inner.equals("byte")) return "BufUtil.readBytes("+bufVar+")";
                if(inner.equals("short")) return "BufUtil.readShortArray("+bufVar+")";
                if(inner.equals("boolean")) return "BufUtil.readBooleanArray("+bufVar+")";
                if(inner.equals("char")) return "BufUtil.readCharArray("+bufVar+")";
                if(inner.equals("float")) return "BufUtil.readFloatArray("+bufVar+")";
                if(inner.equals("double")) return "BufUtil.readDoubleArray("+bufVar+")";
                String elemBuf=childVar(bufVar, "elem");
                return "BufUtil.readObjectArray("+bufVar+", "+javaObjectArrayCreatorExpr(t, "n")+", "+elemBuf+"->"+readValue(elemBuf, inner)+")";
            }
            if(isListLikeType(t)){
                String inner=genericBody(t).trim();
                String elemBuf=childVar(bufVar, "elem");
                String listExpr="BufUtil.readList("+bufVar+", "+elemBuf+"->"+readValue(elemBuf, inner)+")";
                switch (canonicalContainerType(t)){
                    case "ArrayList": return listExpr;
                    case "LinkedList": return "BufUtil.readList("+bufVar+", n->new LinkedList<>(), "+elemBuf+"->"+readValue(elemBuf, inner)+")";
                    default: return listExpr;
                }
            }
            if(isSetLikeType(t)){
                String inner=genericBody(t).trim();
                String elemBuf=childVar(bufVar, "elem");
                String setExpr="BufUtil.readSet("+bufVar+", "+elemBuf+"->"+readValue(elemBuf, inner)+")";
                switch (canonicalContainerType(t)){
                    case "HashSet": return setExpr;
                    case "LinkedHashSet": return "BufUtil.readLinkedSet("+bufVar+", "+elemBuf+"->"+readValue(elemBuf, inner)+")";
                    default: return setExpr;
                }
            }
            if(isQueueLikeType(t)){
                String inner=genericBody(t).trim();
                String elemBuf=childVar(bufVar, "elem");
                return "BufUtil.readCollection("+bufVar+", n->new ArrayDeque<>(n), "+elemBuf+"->"+readValue(elemBuf, inner)+")";
            }
            if(isMapLikeType(t)){
                java.util.List<String> kv=splitTopLevel(genericBody(t), ',');
                if(kv.size()!=2) throw new IllegalArgumentException("Map generic args not valid: "+t);
                String kt=kv.get(0).trim();
                String vt=kv.get(1).trim();
                String keyBuf=childVar(bufVar, "key");
                String valueBuf=childVar(bufVar, "value");
                String mapExpr="BufUtil.readMap("+bufVar+", "+keyBuf+"->"+readValue(keyBuf, kt)+", "+valueBuf+"->"+readValue(valueBuf, vt)+")";
                if("LinkedHashMap".equals(canonicalContainerType(t))){
                    return "BufUtil.readLinkedMap("+bufVar+", "+keyBuf+"->"+readValue(keyBuf, kt)+", "+valueBuf+"->"+readValue(valueBuf, vt)+")";
                }
                if("HashMap".equals(canonicalContainerType(t))){
                    return mapExpr;
                }
                return mapExpr;
            }
            return t+".readFrom("+bufVar+")";
        }
        static String writeValue(String var,String t){
            return writeValue("b", var, t);
            /*
            if(t.equals("int")) return "BufUtil.writeInt(b,"+var+")";
            if(t.equals("long")) return "BufUtil.writeLong(b,"+var+")";
            if(t.equals("byte")) return "BufUtil.writeByte(b,"+var+")";
            if(t.equals("short")) return "BufUtil.writeShort(b,"+var+")";
            if(t.equals("boolean")) return "BufUtil.writeBoolean(b,"+var+")";
            if(t.equals("char")) return "BufUtil.writeChar(b,"+var+")";
            if(t.equals("float")) return "BufUtil.writeFloat(b,"+var+")";
            if(t.equals("double")) return "BufUtil.writeDouble(b,"+var+")";
            if(t.equals("Integer")) return "BufUtil.writeInt(b,"+var+")";
            if(t.equals("Long")) return "BufUtil.writeLong(b,"+var+")";
            if(t.equals("Byte")) return "BufUtil.writeByte(b,"+var+")";
            if(t.equals("Short")) return "BufUtil.writeShort(b,"+var+")";
            if(t.equals("Boolean")) return "BufUtil.writeBoolean(b,"+var+")";
            if(t.equals("Character")) return "BufUtil.writeChar(b,"+var+")";
            if(t.equals("Float")) return "BufUtil.writeFloat(b,"+var+")";
            if(t.equals("Double")) return "BufUtil.writeDouble(b,"+var+")";
            if(t.equals("String")) return "BufUtil.writeString(b,"+var+")";
            if(t.startsWith("List<")){
                String inner=t.substring(5,t.length()-1);
                return "BufUtil.writeList(b,"+var+", (x,y)->"+writeValue("y",inner)+")";
            }
            if(t.startsWith("Set<")){
                String inner=t.substring(4,t.length()-1);
                return "BufUtil.writeSet(b,"+var+", (x,y)->"+writeValue("y",inner)+")";
            }
            if(t.startsWith("Map<")){
                String inside=t.substring(4,t.length()-1);
                java.util.List<String> kv=splitTopLevel(inside, ',');
                if(kv.size()!=2) throw new IllegalArgumentException("Map 婵犵數濮烽弫鍛婃叏閻戣棄鏋侀柛娑橈攻閸欏繘鏌ｉ幋锝嗩棄闁哄绶氶弻娑樷槈濮楀牊鏁鹃梺鍛婄懃缁绘﹢寮婚敐澶婄闁挎繂妫Λ鍕⒑閸濆嫷鍎庣紒鑸靛哺瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁诡垎鍐ｆ寖缂備緡鍣崹鎶藉箲閵忕姭妲堥柕蹇曞Х椤撴椽姊洪崫鍕殜闁稿鎹囬弻娑㈠Χ閸涱垍褔鏌＄仦鍓ф创濠碉紕鍏橀、娆撴偂鎼搭喗浜ょ紓鍌氬€烽懗鑸垫叏娴兼潙纾块柡灞诲劚妗呴梺鍛婃处閸ㄦ澘螞濮椻偓閺屾盯鈥﹂幋婵囩亐闂佸湱顭堥崯鏉戭潖閾忓湱纾兼俊顖濐嚙闂夊秹姊洪崷顓熷殌婵炲眰鍊濋敐鐐剁疀濞戞瑦鍎柣鐔哥懃鐎氼剟宕㈣ぐ鎺撯拺闁告稑锕ｇ欢閬嶆煕閻樺磭澧柍钘夘槼椤﹁鎱ㄦ繝鍌ょ吋鐎规洘甯掗～婵嬵敆婢跺鍋呴梻浣筋嚙鐎涒晝鍠婂鍜佺唵婵せ鍋撻柟顕€绠栭幃婊堟寠婢跺瞼鏉搁梻浣虹帛椤ㄥ懘鎮ч崱妯碱浄婵炲樊浜濋埛鎴︽煕濞戞﹫鍔熼柍钘夘樀閺屻劑寮撮鍡欐殼閻庤娲忛崹铏圭矉閹烘柡鍋撻敐搴′簮闁圭柉娅ｇ槐鎺旂磼閵忕姴绠归梺鐟板暱缁绘濡甸幇顖ｆЬ缂備浇椴哥敮鈥愁嚕婵犳艾唯鐟滃繘寮抽锔界參婵☆垳鍘ч弸搴亜椤撶偞鍋ラ柟铏矒濡啫鈽夊鍡樼秾闂傚倷鑳剁划顖炲箰妤ｅ啫绐楅柟鐗堟緲妗呴梺鍛婃处閸ㄦ壆绮绘繝姘仯闁惧繒鎳撻崝瀣煟濠靛啠鍋撻弬銉︽杸闂佺粯鍔樼亸娆愭櫠濞戞氨纾兼い鏃囧Г瀹曞瞼鈧鍠栭…鐑藉极瀹ュ绀嬫い蹇撴噹婵即姊绘担鍛婂暈闁圭妫欓幏鍛村箵閹哄秴顥氶梻浣告贡閸庛倝宕靛鐐戒汗闁圭儤鎸告禍濂告⒑閸涘﹣绶遍柛銊﹀▕瀵娊濡舵径瀣偓鐢告偡濞嗗繐顏紒鈧崘顏嗙＜妞ゆ棁鍋愮粻濠氭寠濠靛洢浜滈柡鍌濇硶濮ｇ偤鏌￠埀顒勬嚍閵夛絼绨婚梺鍝勫€搁悘婵嬪箖閹达附鐓欓柛蹇曞帶婵秹鏌＄仦鐣屝ら柟鍙夋尦瀹曠喖顢曢妶鍕闂傚倷娴囬鏍窗濡ゅ懏鍋￠柍鍝勬噹缁犵娀鏌ｅΟ缁樸仧闁轰礁妫濋弻宥堫檨闁告挾鍠栧畷娲川閺夋垹顔岄梺鐟版惈濡瑩寮埀顒勬⒒娴ｈ櫣甯涢柛鏃€娲栬灒濠电姴浼ｉ敐鍫涗汗闁圭儤鎸鹃崢浠嬫⒑閸愬弶鎯堥柨鏇樺€濋幃姗€鏁傞柨顖氫壕閻熸瑥瀚粈鍐╀繆閻愯泛袚濞ｅ洤锕幃婊堟嚍閵夛附鐝栭梻渚€鈧偛鑻晶鐗堢箾閸℃劕鐏查柟顔界懇閹粌螣閻撳骸绠ラ梻鍌氬€风欢锟犲矗韫囨洜涓嶉柟瀵稿仦濞呯娀鏌ｅΔ鈧悧鍕濠婂牊鐓涢柛鎰剁到娴滈箖鏌ｉ姀鈺佺伈缂佺粯绻堥悰顕€宕橀妸搴㈡瀹曟﹢鍩℃担鍦偓顓㈡⒒娴ｅ憡鍟炴繛璇х畵瀹曟粌鈽夐埗鍝勬喘婵＄兘濡烽姀鈩冩澑闂備胶绮崝鏇炍熸繝鍌ょ€剁憸蹇曟閹烘鍋愰柧蹇ｅ亜闂夊秹姊洪柅娑氣敀闁告梹鍨垮畷娲焵椤掍降浜滈柟鐑樺灥椤忣亪鏌ｉ幘宕囩闁哄本鐩、鏇㈡晲閸モ晝鏆俊鐐€戦崕铏箾閳ь剟鏌＄仦鍓р槈妞ゎ偅绮撻崺鈧い鎺嗗亾妞ゎ厼娲╅ˇ褰掓煃閵夛附顥堢€规洘锕㈤、娆撳床婢诡垰娲﹂悡鏇㈡煃閳轰礁鏋ら柣鎺嶇矙閺屾稑鈻庤箛鏃戞＆濠殿喖锕ュ钘壩涢崘銊㈡婵﹩鍓﹂弶鎼佹⒒娴ｇ瓔鍤冮柛鐘崇墵瀹曟劙宕稿Δ鈧繚婵炴挻鍩冮崑鎾搭殽閻愮柕顏堚€﹂崸妤€绀嬫い鎺嶇贰濡啴鎮楃憴鍕８闁告梹鍨块妴浣糕枎閹惧磭鐣鹃悷婊冪Ф缁? "+t);
                String kt=kv.get(0).trim(); String vt=kv.get(1).trim();
                return "BufUtil.writeMap(b,"+var+", (x,y)->"+writeValue("y",kt)+", (x,z)->"+writeValue("z",vt)+")";
            }
            if(ENUMS.contains(t)) return "BufUtil.writeInt(b,"+var+".ordinal())";
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2);
                if(inner.equals("int")) return "BufUtil.writeIntArray(b,"+var+")";
                if(inner.equals("long")) return "BufUtil.writeLongArray(b,"+var+")";
                if(inner.equals("byte")) return "BufUtil.writeBytes(b,"+var+")";
                if(inner.equals("short")) return "BufUtil.writeShortArray(b,"+var+")";
                if(inner.equals("boolean")) return "BufUtil.writeBooleanArray(b,"+var+")";
                if(inner.equals("char")) return "BufUtil.writeCharArray(b,"+var+")";
                if(inner.equals("float")) return "BufUtil.writeFloatArray(b,"+var+")";
                if(inner.equals("double")) return "BufUtil.writeDoubleArray(b,"+var+")";
                return "BufUtil.writeObjectArray(b,"+var+", (x,y)->"+writeValue("y",inner)+")";
            }
            return var+".writeTo(b)";
            */
        }
        static String writeValue(String bufVar, String var,String t){
            if(t.equals("int")) return "BufUtil.writeInt("+bufVar+","+var+")";
            if(t.equals("long")) return "BufUtil.writeLong("+bufVar+","+var+")";
            if(t.equals("byte")) return "BufUtil.writeByte("+bufVar+","+var+")";
            if(t.equals("short")) return "BufUtil.writeShort("+bufVar+","+var+")";
            if(t.equals("boolean")) return "BufUtil.writeBoolean("+bufVar+","+var+")";
            if(t.equals("char")) return "BufUtil.writeChar("+bufVar+","+var+")";
            if(t.equals("float")) return "BufUtil.writeFloat("+bufVar+","+var+")";
            if(t.equals("double")) return "BufUtil.writeDouble("+bufVar+","+var+")";
            if(t.equals("Integer")) return "BufUtil.writeInt("+bufVar+","+var+")";
            if(t.equals("Long")) return "BufUtil.writeLong("+bufVar+","+var+")";
            if(t.equals("Byte")) return "BufUtil.writeByte("+bufVar+","+var+")";
            if(t.equals("Short")) return "BufUtil.writeShort("+bufVar+","+var+")";
            if(t.equals("Boolean")) return "BufUtil.writeBoolean("+bufVar+","+var+")";
            if(t.equals("Character")) return "BufUtil.writeChar("+bufVar+","+var+")";
            if(t.equals("Float")) return "BufUtil.writeFloat("+bufVar+","+var+")";
            if(t.equals("Double")) return "BufUtil.writeDouble("+bufVar+","+var+")";
            if(t.equals("String")) return "BufUtil.writeString("+bufVar+","+var+")";
            if(ENUMS.contains(t)) return "BufUtil.writeUInt("+bufVar+","+var+".ordinal())";
            if(isOptionalType(t)){
                String inner=genericBody(t).trim();
                String optBuf=childVar(bufVar, "optional");
                String optVar=childVar(var, "value");
                return "BufUtil.writeOptional("+bufVar+","+var+", ("+optBuf+","+optVar+")->"+writeValue(optBuf, optVar, inner)+")";
            }
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2);
                if(inner.equals("int")) return "BufUtil.writeIntArray("+bufVar+","+var+")";
                if(inner.equals("long")) return "BufUtil.writeLongArray("+bufVar+","+var+")";
                if(inner.equals("byte")) return "BufUtil.writeBytes("+bufVar+","+var+")";
                if(inner.equals("short")) return "BufUtil.writeShortArray("+bufVar+","+var+")";
                if(inner.equals("boolean")) return "BufUtil.writeBooleanArray("+bufVar+","+var+")";
                if(inner.equals("char")) return "BufUtil.writeCharArray("+bufVar+","+var+")";
                if(inner.equals("float")) return "BufUtil.writeFloatArray("+bufVar+","+var+")";
                if(inner.equals("double")) return "BufUtil.writeDoubleArray("+bufVar+","+var+")";
                String elemBuf=childVar(bufVar, "elem");
                String elemVar=childVar(var, "elem");
                return "BufUtil.writeObjectArray("+bufVar+","+var+", ("+elemBuf+","+elemVar+")->"+writeValue(elemBuf, elemVar, inner)+")";
            }
            if(isListLikeType(t) || isSetLikeType(t) || isQueueLikeType(t)){
                String inner=genericBody(t).trim();
                String elemBuf=childVar(bufVar, "elem");
                String elemVar=childVar(var, "elem");
                return "BufUtil.writeCollection("+bufVar+","+var+", ("+elemBuf+","+elemVar+")->"+writeValue(elemBuf, elemVar, inner)+")";
            }
            if(isMapLikeType(t)){
                java.util.List<String> kv=splitTopLevel(genericBody(t), ',');
                if(kv.size()!=2) throw new IllegalArgumentException("Map generic args not valid: "+t);
                String kt=kv.get(0).trim();
                String vt=kv.get(1).trim();
                String keyBuf=childVar(bufVar, "key");
                String valueBuf=childVar(bufVar, "value");
                String keyVar=childVar(var, "key");
                String valueVar=childVar(var, "value");
                return "BufUtil.writeMap("+bufVar+","+var+", ("+keyBuf+","+keyVar+")->"+writeValue(keyBuf, keyVar, kt)+", ("+valueBuf+","+valueVar+")->"+writeValue(valueBuf, valueVar, vt)+")";
            }
            return var+".writeTo("+bufVar+")";
        }
        static String readFixedCountArrayExpr(String bufVar, String t, int fixedCount){
            if("byte[]".equals(t)) return "ByteIO.readFixedCountBytes("+bufVar+", "+fixedCount+")";
            if("int[]".equals(t)) return "ByteIO.readFixedCountIntArray("+bufVar+", "+fixedCount+")";
            if("long[]".equals(t)) return "ByteIO.readFixedCountLongArray("+bufVar+", "+fixedCount+")";
            if("float[]".equals(t)) return "ByteIO.readFixedCountFloatArray("+bufVar+", "+fixedCount+")";
            if("double[]".equals(t)) return "ByteIO.readFixedCountDoubleArray("+bufVar+", "+fixedCount+")";
            throw new IllegalArgumentException("unsupported @fixed array type: "+t);
        }
        static String writeFixedCountArrayExpr(String bufVar, String valueExpr, String t, int fixedCount){
            if("byte[]".equals(t)) return "ByteIO.writeFixedCountBytes("+bufVar+", "+valueExpr+", "+fixedCount+")";
            if("int[]".equals(t)) return "ByteIO.writeFixedCountIntArray("+bufVar+", "+valueExpr+", "+fixedCount+")";
            if("long[]".equals(t)) return "ByteIO.writeFixedCountLongArray("+bufVar+", "+valueExpr+", "+fixedCount+")";
            if("float[]".equals(t)) return "ByteIO.writeFixedCountFloatArray("+bufVar+", "+valueExpr+", "+fixedCount+")";
            if("double[]".equals(t)) return "ByteIO.writeFixedCountDoubleArray("+bufVar+", "+valueExpr+", "+fixedCount+")";
            throw new IllegalArgumentException("unsupported @fixed array type: "+t);
        }
        static String readBorrowedRawArrayExpr(String bufVar, String t){
            if("int[]".equals(t)) return "ByteIO.readBorrowedRawIntArray("+bufVar+")";
            if("long[]".equals(t)) return "ByteIO.readBorrowedRawLongArray("+bufVar+")";
            if("float[]".equals(t)) return "ByteIO.readBorrowedRawFloatArray("+bufVar+")";
            if("double[]".equals(t)) return "ByteIO.readBorrowedRawDoubleArray("+bufVar+")";
            throw new IllegalArgumentException("unsupported @borrow primitive array type: "+t);
        }
        static String readBorrowedRawArrayExpr(String bufVar, String t, String totalCountExpr, String readCountExpr){
            if("int[]".equals(t)) return "ByteIO.readBorrowedRawIntArray("+bufVar+", "+totalCountExpr+", "+readCountExpr+")";
            if("long[]".equals(t)) return "ByteIO.readBorrowedRawLongArray("+bufVar+", "+totalCountExpr+", "+readCountExpr+")";
            if("float[]".equals(t)) return "ByteIO.readBorrowedRawFloatArray("+bufVar+", "+totalCountExpr+", "+readCountExpr+")";
            if("double[]".equals(t)) return "ByteIO.readBorrowedRawDoubleArray("+bufVar+", "+totalCountExpr+", "+readCountExpr+")";
            throw new IllegalArgumentException("unsupported @borrow primitive array type: "+t);
        }
        static String readSampledBorrowedArrayExpr(String bufVar, String t, String totalCountExpr, String indicesExpr){
            if("int[]".equals(t)) return "IntArrayView.of(ByteIO.readSampledFixedIntArray("+bufVar+", null, "+totalCountExpr+", "+indicesExpr+"))";
            if("long[]".equals(t)) return "LongArrayView.of(ByteIO.readSampledFixedLongArray("+bufVar+", null, "+totalCountExpr+", "+indicesExpr+"))";
            if("float[]".equals(t)) return "FloatArrayView.of(ByteIO.readSampledFixedFloatArray("+bufVar+", null, "+totalCountExpr+", "+indicesExpr+"))";
            if("double[]".equals(t)) return "DoubleArrayView.of(ByteIO.readSampledFixedDoubleArray("+bufVar+", null, "+totalCountExpr+", "+indicesExpr+"))";
            throw new IllegalArgumentException("unsupported @borrow primitive array type: "+t);
        }
        static String writeBorrowedRawArrayExpr(String bufVar, String valueExpr, String t){
            if("int[]".equals(t)) return "ByteIO.writeBorrowedRawIntArray("+bufVar+", "+valueExpr+")";
            if("long[]".equals(t)) return "ByteIO.writeBorrowedRawLongArray("+bufVar+", "+valueExpr+")";
            if("float[]".equals(t)) return "ByteIO.writeBorrowedRawFloatArray("+bufVar+", "+valueExpr+")";
            if("double[]".equals(t)) return "ByteIO.writeBorrowedRawDoubleArray("+bufVar+", "+valueExpr+")";
            throw new IllegalArgumentException("unsupported @borrow primitive array type: "+t);
        }
        static String readBorrowedFixedArrayExpr(String bufVar, String t, int fixedCount){
            if("byte[]".equals(t)) return "ByteIO.readBorrowedFixedBytes("+bufVar+", "+fixedCount+")";
            if("int[]".equals(t)) return "ByteIO.readBorrowedFixedIntArray("+bufVar+", "+fixedCount+")";
            if("long[]".equals(t)) return "ByteIO.readBorrowedFixedLongArray("+bufVar+", "+fixedCount+")";
            if("float[]".equals(t)) return "ByteIO.readBorrowedFixedFloatArray("+bufVar+", "+fixedCount+")";
            if("double[]".equals(t)) return "ByteIO.readBorrowedFixedDoubleArray("+bufVar+", "+fixedCount+")";
            throw new IllegalArgumentException("unsupported @borrow @fixed array type: "+t);
        }
        static String writeBorrowedFixedArrayExpr(String bufVar, String valueExpr, String t, int fixedCount){
            if("byte[]".equals(t)) return "ByteIO.writeBorrowedFixedBytes("+bufVar+", "+valueExpr+", "+fixedCount+")";
            if("int[]".equals(t)) return "ByteIO.writeBorrowedFixedIntArray("+bufVar+", "+valueExpr+", "+fixedCount+")";
            if("long[]".equals(t)) return "ByteIO.writeBorrowedFixedLongArray("+bufVar+", "+valueExpr+", "+fixedCount+")";
            if("float[]".equals(t)) return "ByteIO.writeBorrowedFixedFloatArray("+bufVar+", "+valueExpr+", "+fixedCount+")";
            if("double[]".equals(t)) return "ByteIO.writeBorrowedFixedDoubleArray("+bufVar+", "+valueExpr+", "+fixedCount+")";
            throw new IllegalArgumentException("unsupported @borrow @fixed array type: "+t);
        }
        static String readPackedListExpr(String bufVar, String t){
            String inner=genericBody(t).trim();
            if(isIntLikeType(inner)){
                return "ByteIO.readPackedIntList("+bufVar+")";
            }
            if(isLongLikeType(inner)){
                return "ByteIO.readPackedLongList("+bufVar+")";
            }
            String elemBuf=childVar(bufVar, "packedElem");
            if("LinkedList".equals(canonicalContainerType(t))){
                return "ByteIO.readList("+bufVar+", n->new LinkedList<>(), "+elemBuf+"->"+readFixedCursorValue(elemBuf, inner)+")";
            }
            return "ByteIO.readList("+bufVar+", n->"+javaBorrowCollectionExpr(t, "n")+", "+elemBuf+"->"+readFixedCursorValue(elemBuf, inner)+")";
        }
        static String writePackedListExpr(String bufVar, String valueExpr, String t){
            String inner=genericBody(t).trim();
            if(isIntLikeType(inner)){
                return "ByteIO.writePackedIntList("+bufVar+", "+valueExpr+")";
            }
            if(isLongLikeType(inner)){
                return "ByteIO.writePackedLongList("+bufVar+", "+valueExpr+")";
            }
            String elemBuf=childVar(bufVar, "packedElem");
            String elemVar=childVar(valueExpr, "packedValue");
            return "ByteIO.writeCollection("+bufVar+", "+valueExpr+", ("+elemBuf+","+elemVar+")->"+writeFixedCursorValue(elemBuf, elemVar, inner)+")";
        }
        static String readPackedMapExpr(String bufVar, String t){
            List<String> kv=splitTopLevel(genericBody(t), ',');
            if(kv.size()!=2) throw new IllegalArgumentException("Map generic args not valid: "+t);
            String kt=kv.get(0).trim();
            String vt=kv.get(1).trim();
            if(isIntLikeType(kt) && !"LinkedHashMap".equals(canonicalContainerType(t))){
                if(isIntLikeType(vt)){
                    return "ByteIO.readPackedIntIntMap("+bufVar+")";
                }
                if(isLongLikeType(vt)){
                    return "ByteIO.readPackedIntLongMap("+bufVar+")";
                }
                String valueBuf=childVar(bufVar, "packedValue");
                return "ByteIO.readPackedIntObjectMapFast("+bufVar+", "+valueBuf+"->"+readFixedCursorValue(valueBuf, vt)+")";
            }
            String keyBuf=childVar(bufVar, "packedKey");
            String valueBuf=childVar(bufVar, "packedValue");
            return "ByteIO.readMap("+bufVar+", n->"+javaBorrowCollectionExpr(t, "n")+", "
                    +keyBuf+"->"+readFixedCursorValue(keyBuf, kt)+", "
                    +valueBuf+"->"+readFixedCursorValue(valueBuf, vt)+")";
        }
        static String writePackedMapExpr(String bufVar, String valueExpr, String t){
            List<String> kv=splitTopLevel(genericBody(t), ',');
            if(kv.size()!=2) throw new IllegalArgumentException("Map generic args not valid: "+t);
            String kt=kv.get(0).trim();
            String vt=kv.get(1).trim();
            if(isIntLikeType(kt) && !"LinkedHashMap".equals(canonicalContainerType(t))){
                if(isIntLikeType(vt)){
                    return "ByteIO.writePackedIntIntMap("+bufVar+", "+valueExpr+")";
                }
                if(isLongLikeType(vt)){
                    return "ByteIO.writePackedIntLongMap("+bufVar+", "+valueExpr+")";
                }
                String valueBuf=childVar(bufVar, "packedValue");
                String valueVar=childVar(valueExpr, "packedValue");
                return "ByteIO.writePackedIntObjectMap("+bufVar+", "+valueExpr+", ("+valueBuf+","+valueVar+")->"+writeFixedCursorValue(valueBuf, valueVar, vt)+")";
            }
            String keyBuf=childVar(bufVar, "packedKey");
            String valueBuf=childVar(bufVar, "packedValue");
            String keyVar=childVar(valueExpr, "packedKey");
            String valueVar=childVar(valueExpr, "packedValue");
            return "ByteIO.writeMap("+bufVar+","+valueExpr+", ("+keyBuf+","+keyVar+")->"+writeFixedCursorValue(keyBuf, keyVar, kt)+", ("+valueBuf+","+valueVar+")->"+writeFixedCursorValue(valueBuf, valueVar, vt)+")";
        }
        static String readCursorValue(String bufVar, Field f){
            if(isBorrowedBytesField(f)){
                if(f.fixedLength!=null){
                    return "ByteIO.readBorrowedFixedBytes("+bufVar+", "+f.fixedLength+")";
                }
                return "ByteIO.readBorrowedBytes("+bufVar+")";
            }
            if(isBorrowedStringField(f)){
                if(f.fixedLength!=null){
                    return "ByteIO.readBorrowedFixedString("+bufVar+", "+f.fixedLength+")";
                }
                return "ByteIO.readBorrowedString("+bufVar+")";
            }
            if(isBorrowedPrimitiveArrayField(f)){
                if(f.fixedLength!=null){
                    return readBorrowedFixedArrayExpr(bufVar, f.type, f.fixedLength);
                }
                return readBorrowedRawArrayExpr(bufVar, f.type);
            }
            if(isFixedLengthStringField(f)){
                return "ByteIO.readFixedString("+bufVar+", "+f.fixedLength+")";
            }
            if(isFixedCountArrayField(f)){
                return readFixedCountArrayExpr(bufVar, f.type, f.fixedLength);
            }
            if(isPackedPrimitiveListField(f)){
                return readPackedListExpr(bufVar, f.type);
            }
            if(isPackedPrimitiveMapField(f) || isPackedIntKeyObjectMapField(f)){
                return readPackedMapExpr(bufVar, f.type);
            }
            return readCursorValue(bufVar, f.type);
        }
        static String readCursorValue(String bufVar, String t){
            if(t.equals("int")) return "ByteIO.readInt("+bufVar+")";
            if(t.equals("long")) return "ByteIO.readLong("+bufVar+")";
            if(t.equals("byte")) return "ByteIO.readByte("+bufVar+")";
            if(t.equals("short")) return "ByteIO.readShort("+bufVar+")";
            if(t.equals("boolean")) return "ByteIO.readBoolean("+bufVar+")";
            if(t.equals("char")) return "ByteIO.readChar("+bufVar+")";
            if(t.equals("float")) return "ByteIO.readFloat("+bufVar+")";
            if(t.equals("double")) return "ByteIO.readDouble("+bufVar+")";
            if(t.equals("Integer")) return "ByteIO.readInt("+bufVar+")";
            if(t.equals("Long")) return "ByteIO.readLong("+bufVar+")";
            if(t.equals("Byte")) return "ByteIO.readByte("+bufVar+")";
            if(t.equals("Short")) return "ByteIO.readShort("+bufVar+")";
            if(t.equals("Boolean")) return "ByteIO.readBoolean("+bufVar+")";
            if(t.equals("Character")) return "ByteIO.readChar("+bufVar+")";
            if(t.equals("Float")) return "ByteIO.readFloat("+bufVar+")";
            if(t.equals("Double")) return "ByteIO.readDouble("+bufVar+")";
            if(t.equals("String")) return "ByteIO.readString("+bufVar+")";
            if(ENUMS.contains(t)) return t+".fromOrdinal(ByteIO.readUInt("+bufVar+"))";
            if(isOptionalType(t)){
                String inner=genericBody(t).trim();
                String optBuf=childVar(bufVar, "optional");
                return "ByteIO.readOptional("+bufVar+", "+optBuf+"->"+readCursorValue(optBuf, inner)+")";
            }
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2);
                if(inner.equals("int")) return "ByteIO.readIntArray("+bufVar+")";
                if(inner.equals("long")) return "ByteIO.readLongArray("+bufVar+")";
                if(inner.equals("byte")) return "ByteIO.readBytes("+bufVar+")";
                if(inner.equals("short")) return "ByteIO.readShortArray("+bufVar+")";
                if(inner.equals("boolean")) return "ByteIO.readBooleanArray("+bufVar+")";
                if(inner.equals("char")) return "ByteIO.readCharArray("+bufVar+")";
                if(inner.equals("float")) return "ByteIO.readFloatArray("+bufVar+")";
                if(inner.equals("double")) return "ByteIO.readDoubleArray("+bufVar+")";
                String elemBuf=childVar(bufVar, "elem");
                return "ByteIO.readObjectArray("+bufVar+", "+javaObjectArrayCreatorExpr(t, "n")+", "+elemBuf+"->"+readCursorValue(elemBuf, inner)+")";
            }
            if(isListLikeType(t)){
                String inner=genericBody(t).trim();
                String elemBuf=childVar(bufVar, "elem");
                String listExpr="ByteIO.readList("+bufVar+", "+elemBuf+"->"+readCursorValue(elemBuf, inner)+")";
                if("LinkedList".equals(canonicalContainerType(t))){
                    return "ByteIO.readList("+bufVar+", n->new LinkedList<>(), "+elemBuf+"->"+readCursorValue(elemBuf, inner)+")";
                }
                return listExpr;
            }
            if(isSetLikeType(t)){
                String inner=genericBody(t).trim();
                String elemBuf=childVar(bufVar, "elem");
                if("LinkedHashSet".equals(canonicalContainerType(t))){
                    return "ByteIO.readLinkedSet("+bufVar+", "+elemBuf+"->"+readCursorValue(elemBuf, inner)+")";
                }
                return "ByteIO.readSet("+bufVar+", "+elemBuf+"->"+readCursorValue(elemBuf, inner)+")";
            }
            if(isQueueLikeType(t)){
                String inner=genericBody(t).trim();
                String elemBuf=childVar(bufVar, "elem");
                return "ByteIO.readCollection("+bufVar+", n->new ArrayDeque<>(n), "+elemBuf+"->"+readCursorValue(elemBuf, inner)+")";
            }
            if(isMapLikeType(t)){
                java.util.List<String> kv=splitTopLevel(genericBody(t), ',');
                if(kv.size()!=2) throw new IllegalArgumentException("Map generic args not valid: "+t);
                String kt=kv.get(0).trim();
                String vt=kv.get(1).trim();
                String keyBuf=childVar(bufVar, "key");
                String valueBuf=childVar(bufVar, "value");
                String mapExpr="ByteIO.readMap("+bufVar+", "+keyBuf+"->"+readCursorValue(keyBuf, kt)+", "+valueBuf+"->"+readCursorValue(valueBuf, vt)+")";
                if("LinkedHashMap".equals(canonicalContainerType(t))){
                    return "ByteIO.readLinkedMap("+bufVar+", "+keyBuf+"->"+readCursorValue(keyBuf, kt)+", "+valueBuf+"->"+readCursorValue(valueBuf, vt)+")";
                }
                return mapExpr;
            }
            return t+".readFrom("+bufVar+")";
        }
        static String writeCursorValue(String bufVar, String var, Field f){
            if(isBorrowedBytesField(f)){
                if(f.fixedLength!=null){
                    return "ByteIO.writeBorrowedFixedBytes("+bufVar+", "+var+", "+f.fixedLength+")";
                }
                return "ByteIO.writeBorrowedBytes("+bufVar+", "+var+")";
            }
            if(isBorrowedStringField(f)){
                if(f.fixedLength!=null){
                    return "ByteIO.writeBorrowedFixedString("+bufVar+", "+var+", "+f.fixedLength+")";
                }
                return "ByteIO.writeBorrowedString("+bufVar+", "+var+")";
            }
            if(isBorrowedPrimitiveArrayField(f)){
                if(f.fixedLength!=null){
                    return writeBorrowedFixedArrayExpr(bufVar, var, f.type, f.fixedLength);
                }
                return writeBorrowedRawArrayExpr(bufVar, var, f.type);
            }
            if(isFixedLengthStringField(f)){
                return "ByteIO.writeFixedString("+bufVar+", "+var+", "+f.fixedLength+")";
            }
            if(isFixedCountArrayField(f)){
                return writeFixedCountArrayExpr(bufVar, var, f.type, f.fixedLength);
            }
            if(isPackedPrimitiveListField(f)){
                return writePackedListExpr(bufVar, var, f.type);
            }
            if(isPackedPrimitiveMapField(f) || isPackedIntKeyObjectMapField(f)){
                return writePackedMapExpr(bufVar, var, f.type);
            }
            return writeCursorValue(bufVar, var, f.type);
        }
        static String writeCursorValue(String bufVar, String var, String t){
            if(t.equals("int")) return "ByteIO.writeInt("+bufVar+","+var+")";
            if(t.equals("long")) return "ByteIO.writeLong("+bufVar+","+var+")";
            if(t.equals("byte")) return "ByteIO.writeByte("+bufVar+","+var+")";
            if(t.equals("short")) return "ByteIO.writeShort("+bufVar+","+var+")";
            if(t.equals("boolean")) return "ByteIO.writeBoolean("+bufVar+","+var+")";
            if(t.equals("char")) return "ByteIO.writeChar("+bufVar+","+var+")";
            if(t.equals("float")) return "ByteIO.writeFloat("+bufVar+","+var+")";
            if(t.equals("double")) return "ByteIO.writeDouble("+bufVar+","+var+")";
            if(t.equals("Integer")) return "ByteIO.writeInt("+bufVar+","+var+")";
            if(t.equals("Long")) return "ByteIO.writeLong("+bufVar+","+var+")";
            if(t.equals("Byte")) return "ByteIO.writeByte("+bufVar+","+var+")";
            if(t.equals("Short")) return "ByteIO.writeShort("+bufVar+","+var+")";
            if(t.equals("Boolean")) return "ByteIO.writeBoolean("+bufVar+","+var+")";
            if(t.equals("Character")) return "ByteIO.writeChar("+bufVar+","+var+")";
            if(t.equals("Float")) return "ByteIO.writeFloat("+bufVar+","+var+")";
            if(t.equals("Double")) return "ByteIO.writeDouble("+bufVar+","+var+")";
            if(t.equals("String")) return "ByteIO.writeString("+bufVar+","+var+")";
            if(ENUMS.contains(t)) return "ByteIO.writeUInt("+bufVar+","+var+".ordinal())";
            if(isOptionalType(t)){
                String inner=genericBody(t).trim();
                String optBuf=childVar(bufVar, "optional");
                String optVar=childVar(var, "value");
                return "ByteIO.writeOptional("+bufVar+","+var+", ("+optBuf+","+optVar+")->"+writeCursorValue(optBuf, optVar, inner)+")";
            }
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2);
                if(inner.equals("int")) return "ByteIO.writeIntArray("+bufVar+","+var+")";
                if(inner.equals("long")) return "ByteIO.writeLongArray("+bufVar+","+var+")";
                if(inner.equals("byte")) return "ByteIO.writeBytes("+bufVar+","+var+")";
                if(inner.equals("short")) return "ByteIO.writeShortArray("+bufVar+","+var+")";
                if(inner.equals("boolean")) return "ByteIO.writeBooleanArray("+bufVar+","+var+")";
                if(inner.equals("char")) return "ByteIO.writeCharArray("+bufVar+","+var+")";
                if(inner.equals("float")) return "ByteIO.writeFloatArray("+bufVar+","+var+")";
                if(inner.equals("double")) return "ByteIO.writeDoubleArray("+bufVar+","+var+")";
                String elemBuf=childVar(bufVar, "elem");
                String elemVar=childVar(var, "elem");
                return "ByteIO.writeObjectArray("+bufVar+","+var+", ("+elemBuf+","+elemVar+")->"+writeCursorValue(elemBuf, elemVar, inner)+")";
            }
            if(isListLikeType(t) || isSetLikeType(t) || isQueueLikeType(t)){
                String inner=genericBody(t).trim();
                String elemBuf=childVar(bufVar, "elem");
                String elemVar=childVar(var, "elem");
                return "ByteIO.writeCollection("+bufVar+","+var+", ("+elemBuf+","+elemVar+")->"+writeCursorValue(elemBuf, elemVar, inner)+")";
            }
            if(isMapLikeType(t)){
                java.util.List<String> kv=splitTopLevel(genericBody(t), ',');
                if(kv.size()!=2) throw new IllegalArgumentException("Map generic args not valid: "+t);
                String kt=kv.get(0).trim();
                String vt=kv.get(1).trim();
                String keyBuf=childVar(bufVar, "key");
                String valueBuf=childVar(bufVar, "value");
                String keyVar=childVar(var, "key");
                String valueVar=childVar(var, "value");
                return "ByteIO.writeMap("+bufVar+","+var+", ("+keyBuf+","+keyVar+")->"+writeCursorValue(keyBuf, keyVar, kt)+", ("+valueBuf+","+valueVar+")->"+writeCursorValue(valueBuf, valueVar, vt)+")";
            }
            return var+".writeTo("+bufVar+")";
        }
    }
    static class Cs {
        static String generateBufUtil(String ns){
            StringBuilder sb=new StringBuilder();
            sb.append("using System; using System.Buffers; using System.Buffers.Binary; using System.Collections.Concurrent; using System.Collections.Generic; using System.IO; using System.Runtime.CompilerServices; using System.Runtime.InteropServices; using System.Text;\n");
            sb.append("namespace ").append(ns).append(" { public static class BufUtil {\n");
            // Unsafe濠电姷鏁告慨鐑藉极閸涘﹥鍙忛柣鎴ｆ閺嬩線鏌涘☉姗堟敾闁告瑥绻橀弻锝夊箣閿濆棭妫勯梺鍝勵儎缁舵岸寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閹冣挃缂侇噮鍨抽幑銏犫槈閵忕姷顓哄┑鐐叉缁绘帡宕濋幘顔解拺閺夌偞澹嗛ˇ锔姐亜閹存繃顥㈠┑锛勬暬瀹曠喖顢涘槌栧晪闂佽崵濮惧▍锝夊磿閵堝鍊靛┑鐘崇閳锋垿鏌熼懖鈺佷粶濠德ゅГ娣囧﹪顢曢姀鐘虫濡ょ姷鍋涢崯顐ョ亙闂佸憡渚楅崰鏍ㄧ閸濆嫧鏀介柣鎰皺婢э絾绻涙径瀣鐎规洘绻堝畷顐﹀Ψ瑜忛敍婵囩箾鏉堝墽鎮奸柡鍜佸亰瀹曞ジ顢旈崼鐔哄幈婵犵數濮撮崯鎵不閻愮鍋撳▓鍨灕妞ゆ泦鍥х叀濠㈣泛顭悡銉╁箹缁鍤曟い蹇撴噸缁诲棝鏌ｉ幇鍏哥盎闁逞屽墯閸ㄥ灝鐣峰┑鍫滄勃閺夌偞瀵х粙鎴﹀煘閹达箑骞㈡繛鍡樺姈椤旀洟姊绘担鐑樺殌妞ゆ洦鍙冨畷鎴濃槈濮橆収鍋ㄩ梺缁樺姉閸庛倝宕愰悽鍛婄叆婵犻潧妫濋妤€顭胯閸楁娊寮婚敐澶嬫櫇闁逞屽墴閹勭節閸曨剙搴婂┑鐘绘涧椤戝棝宕戦妸鈺傗拻闁割偆鍠庨崹渚€鏌曡箛瀣偓鏍偂閺囩偐鏀介柣妯诲絻閺嗙偤鏌涙繝鍐ㄥ闁逞屽墯椤旀牠宕板Δ鍛畺闁稿本绋愮换鍡涙煠閹间焦娑х紒鍓佸仱閹鏁愭惔鈥愁潻濡ょ姷鍋涢悧鎾愁潖缂佹鐟归柍褜鍓欓…鍥樄闁诡啫鍥у耿婵＄偑鍨虹粙鎴﹀煝鎼淬劌绠ｉ柣妯兼暩閸斿爼姊虹拠鎻掑毐缂傚秴妫濆畷鏉课旈崨顔间簵濠电偞鍨崹娲磹閸洘鐓熼柟閭﹀墰娴犳稒绻涢崨顔界鐎规洦鍨崇划娆忊枎閻愵剙鐦滈梻渚€娼уú銈呯暦椤掑嫬纾归柤濮愬€楅悳濠氭煛閸愩劎澧涢柣鎾寸洴閹﹢鎮欓幓鎺嗘寖濠电偞褰冮悺銊┿€冮妷鈺傚€烽柟纰卞幘閸旂兘姊洪崷顓熷殌閻庢矮鍗抽獮鍐煥閸忓墽鍠撴禍鎼佸冀瑜屾竟鏇熺節閻㈤潧校缁炬澘绉归崺娑㈠箣濠㈡繂缍婂畷妤呮嚃閳哄倸娅橀梻浣告啞鐢﹪宕￠幎钘夎摕婵炴垯鍨归～鍛存煥濠靛棙鍤€闁轰礁娼″铏规嫚閼碱剛顔囬梺鍝ュ枑濞兼瑩锝炶箛鎾佹椽顢旈崟顐ょ崺濠电姷鏁告慨鎶芥嚄閸撲焦宕插〒姘ｅ亾婵﹥妞藉畷銊︾節韫囨埃鍋撶捄銊х＜闁绘ê纾晶铏繆椤愩倕鏋涙慨濠冩そ濡啫霉閵夈儳澧︾€殿喗褰冮…銊╁醇濠靛牆骞堥梻浣侯攰閹活亪姊介崟顖涘亗闁绘ê纾粻楣冩煛婢跺鐒鹃柛搴㈠姍閺屸剝寰勬繝鍕ㄩ梺鍝勭灱閸犳牕鐣峰▎鎾澄ч柛銉㈡櫇濡插洭姊绘担鍛婃儓闁哄牜鍓熼幆鍕敍閻愰潧绁﹀┑鈽嗗灠閻ㄧ兘宕戦幘缁橆棃婵炴垶鐟ラ弳鍫ユ⒑闂堚晝瀵奸柛妤€鍟块～蹇涙惞鐟欏嫬鐝伴梺鐐藉劥濞呮洟鎮甸婊呯＝濞达綀娅ｇ敮娑氱磼鐠囪尙澧﹂柟顔诲嵆椤㈡瑩鏌ㄩ姘闂佹寧绻傛鎼佸几閻斿吋鐓涢柛娑卞枤閻帡鏌熼鐓庢Щ闁宠楠搁埢搴ㄥ箣閻樻劖鍨块弻?
            sb.append("public static bool UseUnsafe { get; set; } = false;\n");
            // 濠电姷鏁告慨鐑藉极閸涘﹥鍙忛柣鎴ｆ閺嬩線鏌涘☉姗堟敾闁告瑥绻橀弻锝夊箣閿濆棭妫勯梺鍝勵儎缁舵岸寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ゆい顓犲厴瀵鏁愭径濠勭杸濡炪倖甯婄拋鏌ュ几濞嗘挻鈷戠紓浣姑粭鈺佲攽椤斿搫鈧骞戦姀鐘闁靛繒濮撮懓鍨攽閳藉棗鐏ユい鏇嗗懎鏋堢€广儱顦伴悡鐔兼煟閺傛寧鎲搁柟铏礈缁辨帡鎮╅搹顐㈢３濡ょ姷鍋涢崯顐ョ亙闂佸憡渚楅崰妤€鈻嶅鍫熺厵闁兼祴鏅炶棢闂佸憡鎸荤换鍫ュ箖濡警鍚嬪璺侯儌閹锋椽姊洪崨濠勭畵閻庢凹鍘介崚濠囨偂楠炵喓鎳撻…銊︽償濠靛牏娉挎俊鐐€ら崑鍕崲濮椻偓楠炴牠宕烽鐔锋瀭闂佸憡娲﹂崑鍡氥亹閹绢喗鈷掑ù锝呮啞閹牓鎮跺鐓庝喊鐎规洘娲栫叅妞ゅ繐瀚崝锕€顪冮妶鍡楃瑐缂佸灈鈧枼鏋旀繝濠傜墛閻撴稓鈧厜鍋撻悗锝庡墰琚ｇ紓鍌欒兌婵敻鎯勯姘煎殨妞ゆ帒瀚崹鍌涖亜閺冨洤袚闁搞倕鐗撳濠氬磼濞嗘劗銈板銈嗘礃閻楃姴鐣烽幎绛嬫晬婵犲﹤瀚惔濠傗攽閻樼粯娑фい鎴濇嚇瀵憡绗熼埀顒勫蓟閻旂厧绀堢憸蹇曟暜濞戙垺鐓曢悗锝庡亜婵秹鏌＄仦璇测偓妤呭窗婵犲洤纭€闁绘劕妯婂楣冩⒒娴ｅ憡鎯堟俊顐ｆ瀹曪繝宕樺顔界稁缂傚倷鐒﹁摫濠殿垱鎸抽弻娑樷槈濮楀牆浼愰梺鍝勬閻楁挸顫忓ú顏勫窛濠电姴鍠氬Λ鏍磽娴ｅ搫校闁瑰憡鎮傞幃楣冩煥鐎ｎ亶鍤ら梺鍝勵槹閸ㄧ敻宕妸銉富闁靛牆妫楁慨鍌炴煕婵犲喚娈滈柟顔惧仦缁轰粙宕ㄦ繛鐐闂備礁鎲＄粙鎴︽晝閵夛箑绶為柛鏇ㄥ灡閻撴洘淇婇姘儓闁抽攱鏌ㄩ…璺ㄦ喆閸曨剛顦伴梺纭呮珪閻楃娀宕洪悙鍝勭畾鐟滃本绔熼弴銏♀拺缂佸娉曠粻鐗堛亜閿旇鐏＄紒鍌氱У閵堬綁宕橀埞鐐闂傚倷绶￠崑鍡涘磻濞戙垺鍤愭い鏍ㄧ⊕濞呯娀鏌熺紒銏犳灍闁绘挻娲熼弻宥囨喆閸曨偄濮㈡繛瀛樼矌閸嬫挻绌辨繝鍥ㄥ€锋い蹇撳閸嬫捇寮介锝嗘婵犵數濮存导鍛般亹閹烘垹鍊炲銈嗗坊閸嬫挾绱掗悪鈧崰妤呭箞閵婏妇绡€闁告洦鍋勯ˉ鎾剁磽閸屾艾鈧悂宕愰幖浣哥９闁归棿绀佺壕褰掓煙闂傚顦︾痪鎯х秺閺岀喖姊荤€靛壊妲紒鐐劤缂嶅﹪寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ょ紓宥咃躬瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁绘劦鍓欓崝銈囩磽瀹ュ拑韬€殿喖顭烽幃銏ゅ礂鐏忔牗瀚介梺璇查叄濞佳勭珶婵犲伣锝夘敊閸撗咃紲闂佺粯鍔﹂崜娆撳礉閵堝棛绡€闁逞屽墴閺屽棗顓奸崨顖滄瀫闂備礁缍婂Λ鍧楁倿閿曞倸纾婚悗锝庡枟閻撴洘銇勯幇鍓佹偧缂佺姵锕㈤弻锝夋偄閺夋垵顬嬬紓浣介哺閹稿骞忛崨鏉戜紶闁告洘鍩婄换婵嬪蓟濞戞鏃€鎷呯化鏇熺亞闁诲孩顔栭崰鏍€﹂悜钘夌畺闁靛繈鍊曠粈鍌炴倶韫囨梻澧悗姘矙濮婅櫣鎷犻崣澶嬪闯闂佽桨鐒﹂幃鍌炲灳閿曞倸閱囬柣鏂垮缁犳碍绻涚€电孝妞ゆ垵妫濋崺?- 濠电姷鏁告慨鐑藉极閸涘﹥鍙忛柣鎴ｆ閺嬩線鏌涘☉姗堟敾闁告瑥绻橀弻锝夊箣閿濆棭妫勯梺鍝勵儎缁舵岸寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ゆい顓犲厴瀵鏁愰崨鍌滃枎閳诲酣骞嗚椤斿嫮绱撻崒娆掑厡濠殿喗鎸抽幃妯侯潩鐠轰綍锕傛煕閺囥劌鏋ら柣銈傚亾闂備礁婀遍崑鎾诲箚鐏炶娇娑㈠Χ閸モ晝锛濇繛杈剧稻瑜板啯绂嶉悙顒傜瘈闁汇垽娼у瓭濠电偠顕滅粻鎾诲箖閿熺姴绀冩い蹇撴噹閺嬫垵鈹戦悩璇у伐闁绘妫涚划鍫ュ醇閻旂寮垮┑鈽嗗灠濞硷繝宕搹鍏夊亾鐟欏嫭绀€鐎殿喖澧庨幑銏犫槈閵忕姷顓洪梺缁樺姇閻忔岸宕宠缁辨挻鎷呯粙娆炬殺闂佺顑冮崐婵嬬嵁閸愩剮鏃堝川椤旇姤鐝抽梺纭呭亹鐞涖儵鍩€椤掑啫鐨洪柡浣圭墬娣囧﹪鎮欓鍕ㄥ亾瑜忕槐鎾愁潩鐠鸿櫣顢呴梺瑙勫劶濡嫮绮婚弽銊ょ箚闁靛牆鍊告禍楣冩⒑瀹曞洨甯涢柟鐟版搐閻ｇ柉銇愰幒婵囨櫓闂佷紮绲介懟顖炴嫃鐎ｎ喗鈷掗柛灞剧懆閸忓本銇勯鐐靛ⅵ妞ゃ垺鐗犲畷銊р偓娑櫳戝▍鍥⒑闂堟侗鐒鹃柛搴㈢懃閳藉濮€閻欌偓濞煎﹪姊洪棃娑氬婵☆偄鐭傞獮蹇撁洪鍛嫼闂侀潻瀵岄崣搴ㄦ倿妤ｅ啯鍊垫繛鎴炲笚濞呭洨绱掗鑲╁ⅵ鐎规洘锕㈤垾锔锯偓锝庝簽缁夋椽鏌熼瑙勬珚鐎规洘锚椤斿繘顢欓柨顖氫壕缁剧偓顒瑀entDictionary
            sb.append("private static readonly ConcurrentDictionary<string, byte[]> _utf8StringCache = new ConcurrentDictionary<string, byte[]>();\n");
            sb.append("private static readonly ConcurrentDictionary<uint, string> _asciiStringCache = new ConcurrentDictionary<uint, string>();\n");
            sb.append("private const int MAX_CACHED_STRING_BYTES=256;\n");
            sb.append("private const int MAX_CACHE_SIZE=1024;\n");
            sb.append("private static int HashCapacity(int size){ if(size<=0) return 4; long capacity=((long)size*4L+2L)/3L; return (int)Math.Max(4L, capacity); }\n");
            sb.append("private static class ListPool<T>{ internal static readonly ConcurrentBag<List<T>> Pool = new ConcurrentBag<List<T>>(); }\n");
            sb.append("private static class HashSetPool<T> where T : notnull { internal static readonly ConcurrentBag<HashSet<T>> Pool = new ConcurrentBag<HashSet<T>>(); }\n");
            sb.append("private static class DictionaryPool<K,V> where K : notnull { internal static readonly ConcurrentBag<Dictionary<K,V>> Pool = new ConcurrentBag<Dictionary<K,V>>(); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static List<T> BorrowList<T>(int capacity){ if(ListPool<T>.Pool.TryTake(out var list)){ list.Clear(); if(list.Capacity<capacity) list.Capacity=capacity; return list; } return new List<T>(capacity); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void RecycleList<T>(List<T>? list){ if(list==null) return; list.Clear(); ListPool<T>.Pool.Add(list); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static HashSet<T> BorrowHashSet<T>(int capacity) where T : notnull { if(HashSetPool<T>.Pool.TryTake(out var set)){ set.Clear(); return set; } return new HashSet<T>(HashCapacity(capacity)); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void RecycleHashSet<T>(HashSet<T>? set) where T : notnull { if(set==null) return; set.Clear(); HashSetPool<T>.Pool.Add(set); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static Dictionary<K,V> BorrowDictionary<K,V>(int capacity) where K : notnull { if(DictionaryPool<K,V>.Pool.TryTake(out var map)){ map.Clear(); return map; } return new Dictionary<K,V>(HashCapacity(capacity)); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void RecycleDictionary<K,V>(Dictionary<K,V>? map) where K : notnull { if(map==null) return; map.Clear(); DictionaryPool<K,V>.Pool.Add(map); }\n");
            // FastBufferWriter缂?
            sb.append("public sealed class FastBufferWriter : IDisposable {\n");
            sb.append("private const int DEFAULT_CAPACITY=1024; private const int MAX_RETAINED_CAPACITY=64*1024; [ThreadStatic] private static FastBufferWriter? CACHED; private byte[] _buffer; private int _written; private bool _returned; private FastBufferWriter(int initialCapacity){ _buffer=ArrayPool<byte>.Shared.Rent(NormalizeCapacity(initialCapacity)); }\n");
            sb.append("private static int NormalizeCapacity(int capacity){ int normalized=Math.Max(DEFAULT_CAPACITY, capacity); int value=1; while(value<normalized) value<<=1; return value; }\n");
            sb.append("private void ReplaceBuffer(int newCapacity, bool copyExisting){ var next=ArrayPool<byte>.Shared.Rent(newCapacity); if(copyExisting && _written!=0) Buffer.BlockCopy(_buffer,0,next,0,_written); ArrayPool<byte>.Shared.Return(_buffer); _buffer=next; }\n");
            sb.append("public static FastBufferWriter Rent(int initialCapacity=DEFAULT_CAPACITY){ var cached=CACHED; if(cached!=null){ CACHED=null; cached._returned=false; cached._written=0; if(cached._buffer.Length<initialCapacity) cached.ReplaceBuffer(NormalizeCapacity(initialCapacity), false); return cached; } return new FastBufferWriter(initialCapacity); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] internal void EnsureWritable(int minWritableBytes){ int required=_written+minWritableBytes; if(required<=_buffer.Length) return; ReplaceBuffer(NormalizeCapacity(required), true); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] internal Span<byte> Reserve(int length){ EnsureWritable(length); var span=_buffer.AsSpan(_written, length); _written+=length; return span; }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] internal void WriteByte(byte value){ EnsureWritable(1); _buffer[_written++]=value; }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] internal void WriteBytes(ReadOnlySpan<byte> source){ EnsureWritable(source.Length); source.CopyTo(_buffer.AsSpan(_written)); _written+=source.Length; }\n");
            // VarInt闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不缂佹﹩娈介柣鎰綑閻忔潙鈹戦鐟颁壕闂備線娼ч悧鍡涘箠閹伴偊鏁婂┑鐘插€甸弨浠嬪箳閹惰棄纾归柟鐗堟緲绾惧鏌熼幆褍顣虫俊顐灦閺岀喖顢涢崱妤冪伇闁告艾顑夊娲传閸曨厾鍔圭紓鍌氱С缁舵岸鎮伴鈧畷鍫曨敆婢跺娅栭梻浣瑰缁诲倸螞瀹€鍕闁告侗鍠氱弧鈧梺姹囧灲濞佳勭濠婂嫪绻嗘い鏍ㄧ啲閺€鑽ょ磼閸屾氨孝妞ゎ厹鍔戝畷鐓庘攽閸偅袨闂傚倷绶氶埀顒傚仜閼活垱鏅堕濮愪簻妞ゅ繐瀚弳锝呪攽閳ュ磭鍩ｇ€规洖宕灃闁逞屽墲閵嗘牜绱撻崒姘偓鎼佸磹閸濄儳鐭撻柡澶嬪殾濞戞鏃堝川椤忎礁浜鹃柨鏇炲€搁悙濠冦亜閹哄秶顦﹀ù婊勭矒濮婅櫣绮欑捄銊ь唶闂佹眹鍔庨崗姗€鐛箛鎾佹椽顢旈崨顏呭闂備胶鍘ч～鏇㈠磹閺囥垹鍑犳繛鎴欏灪閻撴洟鎮楅敐搴濇倣闂婎剦鍓涢埀顒冾潐濞叉粓寮繝姘畺闁靛繈鍊曠粈鍌炴煠濞村娅呮鐐茬墦濮婅櫣鎷犻幓鎺戞瘣缂傚倸绉村Λ婵嗙暦閹达箑骞㈡俊鐐存礉濞咃綁鍩€椤掍胶鈯曢懣褍霉濠婂嫮鐭掗柡灞炬礉缁犳盯濡疯閿涚喎顪冮妶蹇曞埌鐎殿喖澧庨幑銏犫槈閵忊€充簵闁硅壈鎻徊鍧楊敊閹烘挾绡€缁剧増锚婢ф煡鏌熼鐓庘偓瑙勭┍婵犲洤閱囬柡鍥╁仧閸婄偤姊洪崘鍙夋儓闁哥噥鍨拌閹兼番鍨荤弧鈧紒鐐緲椤﹁京澹曢崸妤佺厱閻庯綆鍋勯悘瀵糕偓瑙勬礃閸旀瑥顕ｆ禒瀣垫晣闁绘棁娓圭純鏇熺節瀵伴攱婢橀埀顒佹礋瀹曨垶顢曢敃鈧崙鐘绘煕椤愮喐鍣伴柛瀣崌閻涱噣宕归鐓庮潛婵＄偑鍊х€靛矂宕瑰畷鍥у灊閻犲洤妯婂鈺呮煠閸濄儲鏆╅柛妯哄船椤啴濡堕崱妤€顫囬梺绋匡攻濞茬喎顕ｉ幖浣哥＜婵?- 濠电姷鏁告慨鐑藉极閸涘﹥鍙忛柣鎴ｆ閺嬩線鏌涘☉姗堟敾闁告瑥绻橀弻锝夊箣閿濆棭妫勯梺鍝勵儎缁舵岸寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ゆい顓犲厴瀵鏁愰崨鍌滃枎閳诲酣骞嗚椤斿嫮绱撻崒娆掑厡濠殿喗鎸抽幃妯侯潩鐠轰綍锕傛煕閺囥劌鏋ら柣銈傚亾闂備礁婀遍崑鎾诲箚鐏炶娇娑㈠Χ閸モ晝锛濇繛杈剧稻瑜板啯绂嶉悙顒傜瘈闁汇垽娼у瓭濠电偠顕滅粻鎾诲箖閿熺姴绀冩い蹇撴噹閺嬫垵鈹戦悩璇у伐闁绘妫涚划鍫ュ醇閻旂寮垮┑鈽嗗灠濞硷繝宕搹鍏夊亾鐟欏嫭绀€鐎殿喖澧庨幑銏犫槈閵忕姷顓洪梺缁樺姇閻忔岸宕宠缁辨挻鎷呯粙娆炬殺闂佺顑冮崐婵嬬嵁閸愩剮鏃堝川椤旇姤鐝抽梺纭呭亹鐞涖儵鍩€椤掑啫鐨洪柡浣圭墬娣囧﹪鎮欓鍕ㄥ亾瑜忕槐鎾愁潩鐠鸿櫣顢呴梺瑙勫劶濡嫮绮婚弽銊ょ箚闁靛牆鍊告禍楣冩⒑瀹曞洨甯涢柟鐟版搐閻ｇ柉銇愰幒婵囨櫓闂佷紮绲介懟顖炴嫃鐎ｎ喗鈷掗柛灞剧懆閸忓本銇勯鐐靛ⅵ妞ゃ垺鐗犲畷銊р偓娑櫳戝▍鍥⒑闂堟侗妾у┑鈥虫喘閸╃偛顓奸崨顏呮杸闂佺粯锚瀵爼骞栭幇鐗堚拺闁告鍋為崰姗€鏌″畝瀣瘈鐎规洖鐖奸崺鈩冩媴闁垮鐓曢梻鍌欒兌椤牓鏁冮敃鍌氱；闁靛牆鎷嬪鏍ㄧ箾瀹割喕绨荤紒鐘卞嵆楠炴牕菐椤掆偓閻掔儤鎱ㄦ繝鍌滀虎闁宠鍨块崺銉╁幢濡炲墽鐩庨梺璇插閸戝綊宕ｉ崘銊ф殾婵﹩鍘剧弧鈧梺鍛婂姦閻撳牓宕甸妶澶嬬厽閹兼番鍨婚埊鏇㈡嚕閵堝鐓欏瀣捣鐢稓绱掔紒妯尖姇婵炵厧绻樺畷婊嗩槻闁糕晛鐭傚铏规兜閸滀礁娈濈紓浣虹帛缁诲牓鎮伴鈧畷鍗炩槈濮椻偓閸炲爼姊虹紒妯活梿婵炲拑缍侀、娆撳即閵忥紕鍘介棅顐㈡处濞叉牗绂掑鍫熺厱濠电姴鍊归崯鐐烘煙娓氬灝濮傜€规洘甯掗埞鍐箚瑜屾竟鏇㈡煟閻斿摜鎳冮悗姘煎幘缁牓宕橀鐣屽幈闂侀潧顭堥崕鏌ュ磻閵夛富娈介柣鎰絻閺嗭絽鈹戦鐟颁壕闂備線娼ч悧鍡涘箠瀹ュ洦顫曢柛顐ｆ礃閳锋垿鏌涘☉姗堟敾缂佲偓閳ь剟姊虹拠鑼鐎光偓濮濆本锛傞梻浣芥硶閸犳挻鎱ㄩ悽绋跨厱闁圭儤鍤氳ぐ鎺撴櫜闁告侗鍠楅崕鎾绘煟閵忊晛鐏ラ柛鈺傜墵婵＄敻宕熼锝嗘櫇闂佹寧绻傚Λ娑⑺囨导瀛樺€甸悷娆忓缁€鍐煕閺冣偓閻熲晠鎮伴鈧畷鍫曨敆閳ь剛鐥閹绗熼婊冨辅缂備線浜舵禍璺侯潖缂佹ɑ濯撮柣鎴灻▓灞剧節閳封偓閸涱喗鐝梺鍛婂笚鐢繝銆佸☉銏″€烽柤纰卞墾缁遍亶姊绘笟鈧褑鍣归梺鍛婁緱閸ㄧ晫妲愰弻銉︹拺閻犲洦褰冮崵杈╃磽瀹ュ懏顥炵紒鍌氱Т铻栧ù锝勮濞村嫰姊洪崷顓炲妺妞ゃ劌妫濋幃锟犲即閵忥紕鍘藉┑鈽嗗灡椤戞瑩宕电€ｎ喗鐓涢柍褜鍓熼幊锟犲Χ閸モ晪绱茬紓鍌氬€烽悞锕傗€﹂崶顒€违闁圭儤顨嗛悡鍐喐濠婂牆绀堟慨妯夸含閻瑩鏌熼悜妯镐粶闁逞屽墾缁犳挸鐣烽崼鏇ㄦ晢濞达絿顭堟竟鎺楁煟鎼粹€冲辅闁稿鎹囬弻娑㈠即閵娿儱顫梺鍛婏耿娴滃爼寮婚敐鍡樺劅妞ゆ牗绮庨妶鐑芥⒑閸涘﹥鐓ラ柣顓炲€垮畷娲焵椤掍降浜滈柟鍝勭Х閸忓瞼绱掗悩闈涗沪妞ゃ劊鍎甸幃娆撳箵閹烘挻顔夐梺璇查叄濞佳囧Χ閹间胶宓侀柡宥庡弾閺佸洭鏌ｉ弬鎸庡暈闁绘縿鍔戝濠氬磼濞嗘垹鐛㈤梺閫炲苯澧伴柛瀣洴閹崇喖顢涘☉娆愮彿濡炪倖娲嶉崑鎾绘煛瀹€瀣М闁轰焦鍔欏畷鎯邦槻妤犵偛顑夐幃妤冩喆閸曨剛鈹涚紓浣虹帛缁诲牓鐛崘銊㈡瀻闁规儳纾崢鎼佹⒑缁嬭儻顫﹂柛濠冪墱缁辩偟浠︾憴锝嗘杸闂佺粯蓱椤旀牠寮抽娑楃箚闁圭粯甯楅崵鍥┾偓瑙勬礉椤鈧潧銈稿鍫曞箣閻欌偓閸熷洦淇婇悙顏勨偓鏍礉閹达箑纾规俊銈呮噺閸庢鏌涢銈呮瀭濞存粍绮撻弻锟犲磼濠靛洨銆婂┑顕嗙悼閸嬨倝寮婚敓鐘茬劦妞ゆ帊鑳堕々鐑芥倵閿濆骸浜為柛妯挎閳规垿顢欑粵瀣暥濠碘槅鍋呴〃濠傤嚕缁嬪簱妲堥柕蹇ョ磿閸樹粙妫呴銏℃悙闁挎洏鍎遍埢宥夊川椤旇桨绨诲銈嗘尨閳ь剙鍟挎慨宄邦渻閵堝繘妾柣鎾偓鎰佹綎闁惧繗顫夐弳婊勩亜閺傚灝鈷旂悮锕傛⒒娴ｈ鍋犻柛鏃€鍨圭划濠氬冀椤撶偞鐎梺鍛婂姦閸犳牜澹曢崗鍏煎弿婵☆垱瀵х涵楣冩煛娴ｅ摜绉烘慨濠勭帛缁楃喖鍩€椤掆偓椤洩顦归挊婵囥亜閹惧崬鐏╃痪鎯ф健閺岋紕浠︾拠鎻掑闂佺锕ら悘姘跺Φ閸曨垰绠抽柟瀛樼箥娴犻箖姊洪幎鑺ユ暠闁搞劌娼″濠氭偄閾忓湱鐓撻梺鍓茬厛閸ｎ噣宕濇径宀€纾藉ù锝嗗絻娴滈箖姊洪崨濠傚闁哄倸鍊圭粋宥咁煥閸喓鍘甸梺缁樺灦椤洨绮婚弽顓熺厵闁告劕寮堕弫鐪呴梻鍌氬€搁崐鎼佸磹閹间礁纾归柟闂寸绾惧湱鈧懓瀚崳纾嬨亹閹烘垹鍊炲銈嗗笒椤︿即寮查鍫熷仭婵犲﹤鍟版晥濠电姭鍋撳〒姘ｅ亾婵﹨娅ｇ槐鎺懳熼搹閫涚礃婵犵妲呴崑鍕偓姘緲椤曪綁宕ㄦ繝鍐€撶紓渚囧灡濞叉﹢寮埀顒勬⒒娴ｈ櫣甯涢柨姘舵煟閵堝懏澶勭紒鏃傚枎铻ｉ柤娴嬫櫆閿涘繘姊虹涵鍜佹綈闁告棑绠撳畷闈涒枎閹惧鍘撻柣鐔哥懃鐎氼剟鎮橀幘顔界厵妞ゆ棁顫夊▍濠冾殽閻愬瓨宕屾鐐村浮楠炴﹢骞栭鐐存珒濠电姷顣槐鏇㈠磻閹达箑纾归柟鐗堟緲閻ょ偓绻濇繝鍌滃闁稿鍊块弻銊╂偄閸濆嫅銏ゆ煢閸愵亜鏋涢柡灞诲妼閳规垿宕遍埡鍌傦妇绱撴担鍝勑ｇ紒瀣浮婵＄敻宕熼姘敤闂侀潧顭堥崐妤冩崲娴ｅ湱绠鹃悗鐢殿焾琚ラ梺鍝勬噽婵炩偓妤?
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] internal void WriteVarUInt32(uint value){ EnsureWritable(5); byte[] buffer=_buffer; int index=_written; if(value<0x80){ buffer[index++]=(byte)value; } else if(value<0x4000){ buffer[index++]=(byte)((value&0x7F)|0x80); buffer[index++]=(byte)(value>>7); } else if(value<0x200000){ buffer[index++]=(byte)((value&0x7F)|0x80); buffer[index++]=(byte)(((value>>7)&0x7F)|0x80); buffer[index++]=(byte)(value>>14); } else if(value<0x10000000){ buffer[index++]=(byte)((value&0x7F)|0x80); buffer[index++]=(byte)(((value>>7)&0x7F)|0x80); buffer[index++]=(byte)(((value>>14)&0x7F)|0x80); buffer[index++]=(byte)(value>>21); } else { buffer[index++]=(byte)((value&0x7F)|0x80); buffer[index++]=(byte)(((value>>7)&0x7F)|0x80); buffer[index++]=(byte)(((value>>14)&0x7F)|0x80); buffer[index++]=(byte)(((value>>21)&0x7F)|0x80); buffer[index++]=(byte)(value>>28); } _written=index; }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] internal void WriteVarUInt64(ulong value){ EnsureWritable(10); byte[] buffer=_buffer; int index=_written; while((value&~0x7FUL)!=0UL){ buffer[index++]=(byte)((value&0x7FUL)|0x80UL); value>>=7; } buffer[index++]=(byte)value; _written=index; }\n");
            sb.append("public int WrittenCount => _written; public ReadOnlySpan<byte> WrittenSpan => _buffer.AsSpan(0, _written); public byte[] ToArray(){ byte[] result=GC.AllocateUninitializedArray<byte>(_written); WrittenSpan.CopyTo(result); return result; } [MethodImpl(MethodImplOptions.AggressiveInlining)] public void CopyTo(BinaryWriter writer){ writer.Write(_buffer,0,_written); }\n");
            sb.append("public void Dispose(){ if(_returned) return; _returned=true; _written=0; if(_buffer.Length>MAX_RETAINED_CAPACITY){ ArrayPool<byte>.Shared.Return(_buffer); _buffer=ArrayPool<byte>.Shared.Rent(DEFAULT_CAPACITY); } if(CACHED==null){ CACHED=this; return; } ArrayPool<byte>.Shared.Return(_buffer); _buffer=Array.Empty<byte>(); }\n");
            sb.append("}\n");
            sb.append("public sealed class FastBufferReader : IDisposable {\n");
            sb.append("private static readonly ReadOnlyMemory<byte> EMPTY_MEMORY=ReadOnlyMemory<byte>.Empty; [ThreadStatic] private static FastBufferReader? CACHED; private byte[]? _array; private int _offset; private int _length; private ReadOnlyMemory<byte> _memory; private int _readerIndex; private bool _returned; private FastBufferReader(){ Reset(Array.Empty<byte>()); }\n");
            sb.append("public static FastBufferReader Rent(byte[]? payload){ var cached=CACHED; if(cached!=null){ CACHED=null; cached._returned=false; cached.Reset(payload); return cached; } var reader=new FastBufferReader(); reader.Reset(payload); return reader; }\n");
            sb.append("public static FastBufferReader Rent(ReadOnlyMemory<byte> payload){ var cached=CACHED; if(cached!=null){ CACHED=null; cached._returned=false; cached.Reset(payload); return cached; } var reader=new FastBufferReader(); reader.Reset(payload); return reader; }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] private void EnsureReadable(int length){ if(length<0 || _readerIndex+length>_length) throw new EndOfStreamException(); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] private ReadOnlySpan<byte> RemainingSpan(){ byte[]? array=_array; if(array!=null) return new ReadOnlySpan<byte>(array,_offset+_readerIndex,_length-_readerIndex); return _memory.Span.Slice(_readerIndex,_length-_readerIndex); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] internal byte ReadByte(){ EnsureReadable(1); int index=_readerIndex++; byte[]? array=_array; if(array!=null) return array[_offset+index]; return _memory.Span[index]; }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] internal void ReadBytes(Span<byte> target){ EnsureReadable(target.Length); byte[]? array=_array; if(array!=null){ new ReadOnlySpan<byte>(array,_offset+_readerIndex,target.Length).CopyTo(target); _readerIndex+=target.Length; return; } _memory.Span.Slice(_readerIndex,target.Length).CopyTo(target); _readerIndex+=target.Length; }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] internal ReadOnlySpan<byte> ReadSlice(int length){ EnsureReadable(length); byte[]? array=_array; if(array!=null){ var slice=new ReadOnlySpan<byte>(array,_offset+_readerIndex,length); _readerIndex+=length; return slice; } var fallback=_memory.Span.Slice(_readerIndex,length); _readerIndex+=length; return fallback; }\n");
            // 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤缂嶅﹪寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ょ紓宥咃躬瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁绘劦鍓欓崝銈嗙節閳ь剚娼忛埡鍌ゆ綗闂佸湱鍎ら弻锟犲磻閹剧粯鏅查幖绮光偓鍐差劀闂備焦鎮堕崐褔姊介崟顓熷床婵炴垯鍨圭粻锝夋煟閹存繃鐭楃紒鍓佹暬濮婅櫣绱掑Ο璇茶敿闂佺娴烽弫璇差嚕鐠囨祴妲堥柕蹇婃櫆閺呮繈姊洪幐搴ｇ畵婵炲眰鍔戦幃楣冨传閸曘劍鏂€闂佺偨鍎村▍鏇㈠煝閺囥垺鐓曢柟鎹愭硾閺嬪孩銇勯銏㈢閻撱倖銇勮箛鎾愁仹缂佸崬鐖煎娲川婵犲啫顦╅梺鍛娚戦幃鍌氼嚕閹间礁骞㈡繛鎴炵憿閹疯櫣绱撴担鍓插剱妞ゆ垶鐟╁畷鏇炵暆閸曨剛鍘遍柣搴秵閸嬪懎鐣风仦鐐弿濠电姴鎳忛鐘电磼椤旂晫鎳冮柍璇查叄楠炲棜顦插鐟邦樀濮婄粯鎷呴悜妯烘畬闂佺顕滅换婵嬬嵁閸愵喖纾奸柣鎰摠濞呮粓姊虹紒妯哄缂佷焦鎸冲銊︾鐎ｎ偄鈧敻鏌ㄥ┑鍡樺櫧濞寸姵鐩弻锟犲川椤愩垻浠剧紓浣虹帛缁嬫帒顭囪箛娑樼鐟滃酣宕戣濮婃椽宕崟顒€顎涢梺绋款儏閿曨亝淇婄€涙鐟归柍褜鍓欓悾鐑藉Ω瑜夐崑鎾斥槈濞呰鲸宀搁獮蹇曠磼濡偐顔曢柡澶婄墕婢т粙宕氭导瀛樼厵闁兼亽鍎抽惌宀€绱掗鑺ヮ棃闁诡喗绮岃灒闂傗偓閹邦喚娉块梻鍌欑窔濞佳嚶ㄩ埀顒勬⒒閸曨偄顏╅柣锝夘棑娴狅妇绱掗姀鈽嗗晬闂備胶绮崝姗€顢氬鍫㈠彆妞ゆ帊绶″▓浠嬫煟閹邦厽鍎楅柣鎺嶇矙閺岀喐顦版惔鈾€鏋呴梺鐟扮－婵炩偓妞ゃ垺顨婇崺鈧い鎺戝瀹撲線鎮楅敐搴℃灍闁抽攱甯掗湁闁挎繂娲ら崝瀣煕閵堝倸浜惧┑锛勫亼閸婃牕煤閿曞倸鐭楅柛鎰╁妿閺嗭箓鏌熸潏楣冩闁稿瀚伴弻娑樷攽閸℃浠奸悶姘剧秮濮婄粯绗熼埀顒勫焵椤掑倸浠滈柤娲诲灡閺呭爼骞橀鐣屽幐閻庡厜鍋撻柍褜鍓熷畷浼村冀椤撶偟鐤囬梺鍝勭▉閸樺ジ鎮″☉銏＄厱闁哄洨鍎戝銉︺亜閿旂偓鏆€殿喛顕ч埥澶愬閻樻鍟嬮梺璇查叄濞佳囧箺濠婂牊鍋柛鏇ㄥ亽濞撳鏌曢崼婵嗘殭闁诲浚浜弻锝夋偄閸欏鐝氶梺缁樹緱閸犳岸鍩€椤掑﹦绉甸柛鐘愁殜瀵彃顭ㄩ崼鐔蜂画濠电偛妫欓悷褏绮欐繝姘€垫慨妯挎珪椤ュ鏌嶇憴鍕伌闁诡喗鐟╅幊婊堟濞戞瑩鏁紓鍌氬€峰ù鍥ь嚕閹捐泛鍨濇繛鍡樻尵瀹撲礁顭块懜闈涘闁哄懏鎮傞弻锝呂熼崹顔炬闂佸搫鑻悧鎾愁潖閾忓湱纾兼俊顖氭惈椤酣姊虹粙璺ㄦ槀闁稿﹥绻堥獮鍐晸閻樺啿浜滈梺绋跨箺閸嬫劙宕㈡禒瀣拺闁告繂瀚婵嬫煕鐎ｎ偆娲撮柛鈹惧亾濡炪倖宸婚崑鎾淬亜椤撶姴鍘存鐐插暞閵堬綁宕橀埡浣风紦闂備礁鎲＄粙鎴︽晝閿曞倸鍌ㄩ梻鍫熶緱濞撳鏌曢崼婵囶棡闁绘挾鍠栭弻娑㈠棘鐠恒劎鍔悗瑙勬磸閸庨亶锝炲鍫濈劦妞ゆ巻鍋撴い鏇秮椤㈡岸鍩€椤掆偓閻ｇ兘鎮℃惔妯绘杸闂佺硶鍓濋悷褍鐣烽崫鍕ㄦ斀闁挎稑瀚禍濂告煕婵犲啰澧垫鐐村姈閵堬綁宕橀妸褏宕堕梻浣筋潐瀹曟﹢顢氳閹偤宕归鐘辩盎闂佸湱鍎ら崺鍫澪ｇ粙娆剧唵鐟滄粓宕板Δ鍛﹂柛鏇ㄥ灱閺佸倿鏌涢弴銊ヤ簼婵炲牏绮换婵堝枈濡嘲浜剧€规洖娲ら悡鐔兼倵鐟欏嫭绀€鐎规洦鍓濋悘鎺旂磼缂併垹寮い銉︽尵缁岸宕稿Δ浣叉嫽闂佺鏈悷銊╁礂鐏炰勘浜滄い鎾跺仧婢э附銇勯姀锛勫⒌鐎规洖宕埥澶娢熺喊鍗炴暪婵犵數濮烽弫鍛婃叏閻㈢浼犻幖娣妼閻?
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public ReadOnlyMemory<byte> ReadMemory(int length){ EnsureReadable(length); byte[]? array=_array; if(array!=null){ var mem=_memory.Length==0? new ReadOnlyMemory<byte>(array,_offset+_readerIndex,length): _memory.Slice(_readerIndex,length); _readerIndex+=length; return mem; } return _memory.Slice(_readerIndex+_offset,length); }\n");
            // VarInt闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳婀遍埀顒傛嚀鐎氼參宕崇壕瀣ㄤ汗闁圭儤鍨归崐鐐差渻閵堝棗绗掓い锔垮嵆瀵煡顢旈崼鐔蜂画濠电姴锕ら崯鎵不缂佹﹩娈介柣鎰綑閻忔潙鈹戦鐟颁壕闂備線娼ч悧鍡涘箠閹伴偊鏁婂┑鐘插€甸弨浠嬪箳閹惰棄纾归柟鐗堟緲绾惧鏌熼幆褍顣虫俊顐灦閺岀喖顢涢崱妤冪伇闁告艾顑夊娲传閸曨厾鍔圭紓鍌氱С缁舵岸鎮伴鈧畷鍫曨敆婢跺娅栭梻浣瑰缁诲倸螞瀹€鍕闁告侗鍠氱弧鈧梺姹囧灲濞佳勭濠婂嫪绻嗘い鏍ㄧ啲閺€鑽ょ磼閸屾氨孝妞ゎ厹鍔戝畷鐓庘攽閸偅袨闂傚倷绶氶埀顒傚仜閼活垱鏅堕濮愪簻妞ゅ繐瀚弳锝呪攽閳ュ磭鍩ｇ€规洖宕灃闁逞屽墲閵嗘牜绱撻崒姘偓鎼佸磹閸濄儳鐭撻柡澶嬪殾濞戞鏃堝川椤忎礁浜鹃柨鏇炲€搁悙濠冦亜閹哄秶顦﹀ù婊勭矒濮婅櫣绮欑捄銊ь唶闂佹眹鍔庨崗姗€鐛箛鎾佹椽顢旈崨顏呭闂備胶鍘ч～鏇㈠磹閺囥垹鍑犳繛鎴欏灪閻撴洟鎮楅敐搴濇倣闂婎剦鍓涢埀顒冾潐濞叉粓寮繝姘畺闁靛浚婢€閻掑﹤霉閿濆牜娼愰柡澶婃啞娣囧﹪鎮欓鍕ㄥ亾閺嵮屾綎濠电姵鑹剧壕濠氭煙閹规劦鍤欐慨瑙勭叀閺屽秹宕崟顒€娅ら梺缁樻尭閸熶即骞夌粙娆剧叆闁割偅绻勯ˇ顓炩攽椤旂煫顏勭暦椤掑嫬鍑犻柛顐熸噰閸嬫捇鐛崹顔煎闂佺娅曢崝娆忣嚕閹惰棄骞㈡繛鎴炵懅閸樼敻姊婚崒姘偓鎼侇敋椤撯懞鍥晜閸撗咃紲闂佺粯锚绾绢厽鏅堕柆宥嗙厵闁告瑥顦伴崐鎰版煙椤斻劌娲ら柋鍥ㄧ節闂堟稓澧㈤柟鍐叉濮婄粯鎷呴搹鐟扮闂佸憡姊瑰ú鐔煎极閸愵噮鏁傞柛顐ｇ箚閹芥洟姊洪崫鍕窛闁哥姵鎸剧划缁樸偅閸愨晝鍘卞銈庡幗閸ㄧ敻寮稿☉妯锋斀妞ゆ柨鎼悘顔剧磼鏉堛劍灏伴柟宄版噺椤︾増鎯旈敐鍥у簥濠碉紕鍋戦崐鎴﹀磿閺屻儱绠伴柟顖ｇ仜閿濆绠涢柡澶庢硶椤旀帡鏌ｆ惔銏⑩姇闁挎碍銇勬惔銏″磳婵﹥妞介幊锟犲Χ閸涱喚鈧偓绻濋姀銏″殌婵☆偅绋撻崚鎺撶節閸ャ劌鈧鏌ら幁鎺戝姉闁归攱妞介弻锝夋偐閸欏鈹涢柣蹇撶箲閻熝呭垝婵犳碍鍤戞い鎺戝€婚敍婊堟煟閻樺弶绌块悘蹇旂懄閺呭爼寮撮姀锛勫弳闂佸搫娲﹂敋闁诲浚浜炵槐鎺懳旈崘銊︾亪闂佺硶鏂侀崑鎾愁渻閵堝棗鍧婇柛瀣尰閵囧嫰顢曢姀銏㈩啋闂佸湱鍘х紞濠傜暦閻戠瓔鏁囬柣姗€娼ч獮鍡涙⒒娴ｈ棄鍚瑰┑顔肩仛缁傚秵绂掔€ｎ亞顦柟鍏兼儗閻撳牓寮繝鍌ょ唵閻犻缚娅ｉ悘杈╃棯閹规劖顥夐棁澶愭煥濠靛棭妯堟俊顐ｅ灦閵囧嫰鏁傞悡搴喘缂備浇椴搁幐鎼侇敇婵傜妞介柛鎰靛幖閹牓姊绘笟鈧褍螞閺冨倹顐芥慨姗嗗墻閸ゆ洘銇勯弴妤€浜鹃悗瑙勬礈閸樠囧煘閹达箑骞㈡慨姗堢到娴滈箖鏌涜椤ㄥ棝藟婵犲啨浜滈柟鎵虫櫅閻忣亜顭跨捄鍝勵伃闁哄瞼鍠栭獮鏍ㄦ媴鐟欏嫭娈哥紓?
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] internal int ReadVarUInt32(){ byte[]? array=_array; if(array!=null){ int index=_offset+_readerIndex; int end=_offset+_length; if(index<end){ int b0=array[index]; if((b0&0x80)==0){ _readerIndex+=1; return b0; } if(index+1<end){ int result=(b0&0x7F)|((array[index+1]&0x7F)<<7); if((array[index+1]&0x80)==0){ _readerIndex+=2; return result; } result|=((array[index+2]&0x7F)<<14); if(index+2<end && (array[index+2]&0x80)==0){ _readerIndex+=3; return result; } result|=((array[index+3]&0x7F)<<21); if(index+3<end && (array[index+3]&0x80)==0){ _readerIndex+=4; return result; } if(index+4<end){ result|=(array[index+4]&0x0F)<<28; if((array[index+4]&0xF0)==0){ _readerIndex+=5; return result; } throw new InvalidDataException(\"unsigned varint is too long\"); } } } } return ReadVarUInt32Slow(); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] private int ReadVarUInt32Slow(){ int num=0; int result=0; while(true){ byte b=ReadByte(); result|=(b&0x7F)<<(7*num); if((b&0x80)==0) return result; num++; if(num>=5) throw new InvalidDataException(\"unsigned varint is too long\"); } }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] internal ulong ReadVarUInt64(){ byte[]? array=_array; if(array!=null){ int index=_offset+_readerIndex; int end=_offset+_length; if(index<end){ ulong result=0; int shift=0; int i=0; while(index+i<end&&i<10){ byte b=array[index+i]; result|=(ulong)(b&0x7F)<<shift; if((b&0x80)==0){ _readerIndex+=i+1; return result; } i++; shift+=7; } } } return ReadVarUInt64Slow(); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] private ulong ReadVarUInt64Slow(){ int num=0; ulong result=0; while(true){ byte b=ReadByte(); result|=(ulong)(b&0x7F)<<(7*num); if((b&0x80)==0) return result; num++; if(num>=10) throw new InvalidDataException(\"unsigned varlong is too long\"); } }\n");
            sb.append("public int Remaining => _length-_readerIndex; public void Reset(byte[]? payload){ _array=payload ?? Array.Empty<byte>(); _offset=0; _length=_array.Length; _memory=EMPTY_MEMORY; _readerIndex=0; }\n");
            sb.append("public void Reset(ReadOnlyMemory<byte> payload){ _readerIndex=0; if(MemoryMarshal.TryGetArray(payload, out ArraySegment<byte> segment) && segment.Array!=null){ _array=segment.Array; _offset=segment.Offset; _length=segment.Count; _memory=EMPTY_MEMORY; return; } _array=null; _offset=0; _length=payload.Length; _memory=payload; }\n");
            sb.append("public void Dispose(){ if(_returned) return; _returned=true; _array=Array.Empty<byte>(); _offset=0; _length=0; _memory=EMPTY_MEMORY; _readerIndex=0; if(CACHED==null){ CACHED=this; } }\n");
            sb.append("}\n");
            // String缂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤缂嶅﹪寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ょ紓宥咃躬瀵鎮㈤崗灏栨嫽闁诲酣娼ф竟濠偽ｉ鍓х＜闁绘劦鍓欓崝銈囩磽瀹ュ拑韬€殿喖顭烽幃銏ゅ礂鐏忔牗瀚介梺璇查叄濞佳勭珶婵犲伣锝夘敊閸撗咃紲闂佺粯鍔﹂崜娆撳礉閵堝棛绡€闁逞屽墴閺屽棗顓奸崨顖滄瀫闂備礁缍婂Λ鍧楁倿閿曞倸纾婚悗锝庡枟閻撴洘銇勯幇鍓佹偧缂佺姵锕㈤弻锝夋偄閺夋垵顬嬬紓浣介哺閹稿骞忛崨鏉戜紶闁告洘鍩婄换婵嬪蓟濞戞鏃€鎷呯化鏇熺亞闁诲孩顔栭崰鏍€﹂悜钘夌畺闁靛繈鍊曠粈鍌炴倶韫囨梻澧悗姘矙濮婅櫣鎷犻崣澶嬪闯闂佽桨鐒﹂幃鍌炲灳閿曞倸閱囬柣鏂垮缁犳碍绻涚€电孝妞ゆ垵妫濋崺娑㈠箣閻樼數锛滈柣搴秵閸嬫帡宕曢妷鈺傜厱閹艰揪绱曟晥闂佸搫鏈粙鎺旀崲濠靛绀冩い蹇撴閺変粙鏌ｆ惔銏╁晱闁哥姵鐗犻垾锕€鐣￠柇锕€娈ㄩ梺鍦檸閸犳宕戦崟顖涚叄闊洦鎸荤拹鈩冧繆閹绘帗鍠樻慨濠勭帛缁绘繃鎯旈垾鑼泿婵犵數鍋犻婊呯不閹寸姴寮叉繝鐢靛Т閿曘倝鎮ч崱妞パ囧蓟閵夛妇鍘藉┑鈽嗗灣閸庛倝鍩㈤弴鐔虹闁圭偓鍓氶悡濂告煛鐏炵偓绀嬬€规洜鍘ч埞鎴﹀炊閼哥绱掔紓鍌氬€风拋鏌ュ磻閹炬剚鐔嗛悹杞拌閻擃剚绻涢幘鎰佺吋闁哄备鍓濆鍕偓锝庝簽娴犳悂姊洪崫銉ユ灁闁稿鍠撳Σ鎰板箳閹寸姵娈曢梺鍛婂姦閸犳牠骞楅悽鐢电＝濞达綀娅ｇ敮娑氱磼鐠囪尙澧曢柣锝囧厴瀹曞ジ寮撮悢閿嬬杺闂備礁澹婇悡鍫ュ磻閸涘瓨鍋熸い鎰ㄦ噰閺€浠嬫煟濡澧柛鐔风箻閺屾盯鎮╅幇浣圭杹闂佽桨绀侀崯鎾春閿熺姴宸濇い鎾跺仜娴滆泛鈹戦悙宸殶濠殿喗鎸抽、鏍川椤栨凹妫滈梺绉嗗嫷娈曢柣鎾寸懇閺岋綁骞囬棃娑橆潻婵犫拃灞界仭缂?- 濠电姷鏁告慨鐑藉极閸涘﹥鍙忛柣鎴ｆ閺嬩線鏌涘☉姗堟敾闁告瑥绻橀弻锝夊箣閿濆棭妫勯梺鍝勵儎缁舵岸寮婚悢鍏尖拻閻庨潧澹婂Σ顔剧磼閻愵剙鍔ゆい顓犲厴瀵鏁愰崨鍌滃枎閳诲酣骞嗚椤斿嫮绱撻崒娆掑厡濠殿喗鎸抽幃妯侯潩鐠轰綍锕傛煕閺囥劌鏋ら柣銈傚亾闂備礁婀遍崑鎾诲箚鐏炶娇娑㈠Χ閸モ晝锛濇繛杈剧稻瑜板啯绂嶉悙顒傜瘈闁汇垽娼у瓭濠电偠顕滅粻鎾诲箖閿熺姴绀冩い蹇撴噹閺嬫垵鈹戦悩璇у伐闁绘妫涚划鍫ュ醇閻旂寮垮┑鈽嗗灠濞硷繝宕搹鍏夊亾鐟欏嫭绀€鐎殿喖澧庨幑銏犫槈閵忕姷顓洪梺缁樺姇閻忔岸宕宠缁辨挻鎷呯粙娆炬殺闂佺顑冮崐婵嬬嵁閸愩剮鏃堝川椤旇姤鐝抽梺纭呭亹鐞涖儵鍩€椤掑啫鐨洪柡浣圭墬娣囧﹪鎮欓鍕ㄥ亾瑜忕槐鎾愁潩鐠鸿櫣顢呴梺瑙勫劶濡嫮绮婚弽銊ょ箚闁靛牆鍊告禍楣冩⒑瀹曞洨甯涢柟鐟版搐閻ｇ柉銇愰幒婵囨櫓闂佷紮绲介懟顖炴嫃鐎ｎ喗鈷掗柛灞剧懆閸忓本銇勯鐐靛ⅵ妞ゃ垺鐗犲畷銊р偓娑櫳戝▍鍥⒑闂堟侗鐒鹃柛搴㈢懃閳藉濮€閻欌偓濞煎﹪姊洪棃娑氬婵☆偄鐭傞獮蹇撁洪鍛嫼闂侀潻瀵岄崣搴ㄦ倿妤ｅ啯鍊垫繛鎴炲笚濞呭洨绱掗鑲╁ⅵ鐎规洘锕㈤垾锔锯偓锝庝簽缁夋椽鏌熼瑙勬珚鐎规洘锚椤斿繘顢欓柨顖氫壕缁剧偓顒瑀entDictionary
            sb.append("private static byte[] GetCachedUtf8Bytes(string value){ if(value.Length==0||value.Length>MAX_CACHED_STRING_BYTES) return Array.Empty<byte>(); return _utf8StringCache.GetOrAdd(value, v=>{ int n=Encoding.UTF8.GetByteCount(v); if(n>MAX_CACHED_STRING_BYTES) return Array.Empty<byte>(); byte[] bytes=GC.AllocateUninitializedArray<byte>(n); Encoding.UTF8.GetBytes(v,bytes); if(_utf8StringCache.Count>MAX_CACHE_SIZE){ _utf8StringCache.Clear(); } return bytes; }); }\n");
            sb.append("private static bool TryGetCachedAsciiString(ReadOnlySpan<byte> utf8, out uint hash, out string? value){ hash=0; value=null; if(utf8.Length==0||utf8.Length>MAX_CACHED_STRING_BYTES) return false; hash=2166136261U; for(int i=0;i<utf8.Length;i++){ byte b=utf8[i]; if((b&0x80)!=0) return false; hash=(hash^b)*16777619U; } return _asciiStringCache.TryGetValue(hash, out value); }\n");
            sb.append("private static void CacheAsciiString(uint hash, string value){ if(value.Length==0||value.Length>MAX_CACHED_STRING_BYTES) return; _asciiStringCache[hash]=value; if(_asciiStringCache.Count>MAX_CACHE_SIZE) _asciiStringCache.Clear(); }\n");
            // 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鐐劤缂嶅﹪寮婚敐澶婄闁挎繂鎲涢幘缁樼厱闁靛牆鎳庨顓㈡煛鐏炲墽娲存鐐达耿閹崇娀顢楅埀顒佷繆娴犲鈷戠紒瀣硶缁犺尙绱掗鑺ュ磳鐎殿喛顕ч埥澶愬閳哄倹娅囬梻浣瑰缁诲倸螞濞戔懞鍥Ψ閳哄倵鎷哄┑顔炬嚀濞层倝鍩€椤掍胶绠樼紒顔碱煼瀵粙顢橀悙鑸垫啺闂備焦瀵х换鍌炈囬婊呬笉婵炴垶鐟ｆ禍婊堟煙閹规劖纭剧悮銊╂⒑缁嬫鍎戦柛瀣ㄥ€曢～蹇撁洪鍕炊闂佸憡娲熷褔宕滈幍顔剧＝濞达絽澹婂Σ鍝ョ磽瀹ュ拑宸ユい鏇稻缁绘繂顫濋鈹炬櫊閺屾洘寰勯崼婵堜痪闂佸搫鍊甸崑鎾绘⒒閸屾瑨鍏岀痪顓℃硾椤﹨顦圭€殿喗褰冮…銊╁幢濡炶浜鹃柟缁㈠枟閳锋帡鏌涚仦鍓ф噮缂佹劖妫冮弻宥堫檨闁告挻鐩畷鎴濃槈閵忊€虫濡炪倖鐗滈崑娑㈠磼閵娾晜鐓曟繝闈涘閸斻倗鐥幆褋鍋㈤柡灞诲€濆褰掑箛椤斾勘鍋″┑鐘灱椤鏁冮姀銈呰摕闁挎繂顦伴弲鏌ユ煕閵夋垵鍟粻锝呪攽閻樻鏆柛鎾寸箞楠炲啴宕掗悙鍙夋К闂佹枼鏅涢崯鎵姬閳ь剟姊洪崨濠傚闁哄懏绮撻幃鐢割敃閳垛晜鏂€闂佺粯锚閻忔岸寮抽埡浣叉斀妞ゆ棁濮ょ亸锕傛煛娴ｇ鏆ｅ┑陇鍩栧鍕節閸曨剛鍔稿┑锛勫亼閸婃牠鎮уΔ鍛槬闁告劦鍠栭崙鐘绘煛閸愶絽浜鹃梺闈涙搐鐎氫即鐛崶顒€閱囬柡鍥╁櫏閳ь剙绉瑰娲箮閼恒儲鏆犻梺鎼炲妼濞尖€愁嚕婵犳艾围闁糕剝锚瀵潡姊鸿ぐ鎺戜喊闁稿繑锕㈠畷鎴﹀箻鐠囨彃宓嗛梺缁樺姇閻忔岸骞婇崘鈺冪瘈闁汇垽娼у暩闂佽桨鐒﹂幃鍌氱暦閹达附鍊烽柣銏㈡暩閻掗箖姊洪崷顓℃闁哥姵鐗犻幃鍧楁倷椤掑倻鐦堥梻鍌氱墛缁嬫帒顔忓┑鍥ヤ簻闁冲搫瀚惃铏圭磼鏉堛劌绗ч柍褜鍓ㄧ紞鍡樼閻愬樊娼╅柕濞炬櫆閻撴盯鎮楅敐搴濋偗闁告瑥瀚伴弻鈥崇暆閳ь剟宕伴弽顓犲祦闁糕剝鍑瑰Σ楣冩⒑閹稿海鈽夌紒澶婄秺瀵鈽夐姀鐘电潉闂佽鍎虫晶搴ㄥ汲閻樺樊娓婚柕鍫濈凹缁ㄥ鏌涢悢椋庢憼濞ｅ洤锕畷濂稿即閻愯尪鈧灝鈹戞幊閸婃洟宕导鏉戠疇闁糕剝绋掗埛鎴︽偣閸ャ劌绲绘い鎺嬪灲閺屻倛銇愰幒鏃傛毇閻庤娲橀悡锟犲箠閻樿绀堝ù锝夘棑缂嶅秹姊婚崒姘偓鐑芥嚄閸洏鈧焦绻濋崶褏顔屽銈呯箰閻楀﹪鍩涢弮鍫熺厱闁哄洢鍔岄悘锟犳煕鐎ｎ亜鈧湱鎹㈠☉銏犲耿婵°倓鑳堕々鏉库攽閻愬瓨灏い顓犲厴楠炲啫螖閸涱喗娅滈柟鐓庣摠缁诲牓宕戝Δ鈧—鍐Χ閸涱収鍔夊銈冨妼閿曘倝鎮鹃悜绛嬫晬闁绘劘灏欓鎰箾鏉堝墽鍒伴柛妯荤矒楠炲繐煤椤忓懎浠┑鐘诧工閹冲酣銆傛總鍛婂仺妞ゆ牗顨嗗▍鍛存懚閻愮儤鐓欓悗娑欘焽婵″洭鏌￠埀顒佺鐎ｎ偆鍘介梺纭呭焽閸斿秴鈻嶉崨顒肩懓顭ㄩ崨顓濆婵烇絽娲ら敃顏呬繆閸洖鐐婃い顒夊枔閸庣敻寮诲☉姘ｅ亾閿濆骸澧ù鐘洪哺椤ㄣ儵鎮欓弶鎴犱紝閻庤娲栭悥濂搞€佸Δ鍛＜婵ü绌堕崑濠囨⒒閸屾瑧顦︽繝鈧柆宥呯？闁靛牆顦埀顒€鍟村畷銊╊敍濠娾偓缁楀绻濋悽闈浶ｇ痪鏉跨Ч閹€斥枎閹寸姵锛忛梺鍝勵槸閻忔繈鎳滅憴鍕垫闁绘劖娼欏ù顕€鏌″畝鈧崰鎾诲焵椤掑倹鏆╂い顓炵墦瀹曘垻鈧稒蓱閸欏繐鈹戦悩鎻掍簽闁绘捁鍋愰埀顒冾潐濞叉鏁幒妤嬬稏婵犻潧顑愰弫鍕煢濡警妲峰瑙勬礃娣囧﹪鎮欏顔煎壈濠电偛顕崗姗€骞嗗畝鍕耿婵＄偛鐨烽崑鎾诲箳閹搭厽鍍甸梺鎸庣箓閹冲秵绔熼弴鐔剁箚闁靛牆娲ゅ暩闂佺顑囬崑銈夊箖瑜旈幃鈺佇ч崶锝呬壕濞撴埃鍋撶€殿喗鎸虫慨鈧柍鈺佸暞閻濇牠姊绘笟鈧埀顒傚仜閼活垱鏅堕弶娆剧唵閻熸瑥瀚粈瀣煙椤旀儳鍘存鐐诧攻缁绘繈宕掑鍛呫劑姊虹拠鈥虫灀闁哄懏鐩、娆掔疀濞戣鲸鏅╅梺缁樻尭妤犳瓕鐏囩紓鍌氬€搁崐鎼佸磹閹间礁纾归柛婵勫劤閻捇鏌ｉ姀鐘冲暈闁稿顑夐弻锟犲炊閿濆棗娅氶梺閫炲苯澧惧┑鈥虫喘楠炴垿宕熼姣尖晠鏌ㄩ弴妤€浜剧紒鍓ц檸閸ㄨ泛顫忛搹鍦＜婵☆垵娅ｆ禒鎼佹煢閸愵喕鎲鹃柡宀€鍠栭幃婊堝箣閹烘挸鏋ゆ繝娈垮枛閿曘劌鈻嶉敐鍥у灊婵炲棙鍨跺畷澶愭煏婵炑冭嫰閺佽偐绱撻崒姘偓椋庢閿熺姴绐楁俊銈呮噹缁犳煡鏌涢妷鎴濊嫰濞堛劑鏌ｉ悩鍙夊缂佷焦娼欏嵄闁割偁鍎查悡蹇涚叓閸パ嶆敾婵炲懎妫濋弻锟犲川椤斿墽鐤勫┑顔硷攻濡炰粙鐛幇顓熷劅闁挎繂娲ㄩ弳銈嗙節绾版ɑ顫婇柛瀣嚇閹嫰顢涘┑鍥舵（闂傚倷绶氬褑澧濋梺鍝勬噺缁捇宕哄☉銏犵闁挎梻鏅崢鍗炩攽閻樼粯娑ф俊顐ｎ殜椤㈡棃顢旈崟銊︽杸濡炪倖鐗楅崫搴ㄥ磻閵忋倖鐓涢悘鐐插⒔濞插瓨顨ラ悙鎼劷闁圭懓瀚伴幃婊兾熼梻鎾仐闂備浇顕х€涒晠顢欓弽顓炵獥婵炴垯鍩勯弫瀣喐閺冨牆鏄ラ柕澶涚畱缁剁偤鏌熼柇锕€澧绘繛鐓庯躬濮婃椽宕橀崣澶嬪創闂佺锕﹂幊鎾诲煝瀹ュ鐐婃い鎺嶈閹风粯绻涙潏鍓ф偧闁硅櫕鎹囬、姘煥閸涱垳锛滈柣鐘叉处瑜板啴鍩€椤掆偓閹芥粓宕ｉ崨瀛樷拺闂傚牊鍗曢崼銉ョ柧婵犲﹤鐗嗙壕濠氭煕瀹€鈧崑鐐烘偂閺囥垺鍊堕柣鎰絻閳锋棃鏌ｉ鐐电伇闁汇儺浜獮鎴﹀箛椤撗勵棄闂傚倸娲らˇ鐢稿蓟閵娿儮鏀介柛鈩兠▍銈夋⒑鐠団€崇仩闁绘绻戠粋宥咁潰瀹€鈧悿鈧梺鍝勬川婵兘鏁嶅┑鍥╃闁瑰墽顥愭竟妯荤箾鐏炲倸鈧繂顕ｉ幖浣瑰亜闁稿繗鍋愰崢浠嬫⒑缂佹ɑ鐓ョ€殿喛娉涢埢宥夊川椤旇桨绨婚梺鍝勬祩娴滅偟绮欓懡銈囩＜缂備焦顭囩粻鎾淬亜椤愶絿绠炴い銏★耿閹垽宕妷銉ь槮婵犵數濮烽弫鍛婃叏娴兼潙鍨傞柣鎾崇岸閺嬫牗绻涢幋娆忕労闁轰礁瀚伴弻娑㈠焺閸愶缚绮堕梺鍝勵儏缁夌數鎹㈠┑鍥╃瘈闁稿本绮岄·鈧梻浣瑰▕閺€閬嶅垂閸︻厽顫曢柟鐑橆殢閺佸秵绻涢幋鐐垫噮妞わ负鍎茬换婵嬪煕閳ь剟宕熼鐐茬哗闂備礁鎼惌澶岀礊娓氣偓楠炲啴濡舵径濠勶紲濠电姴锕ら幊蹇撯枔婵傚憡鐓熼幖娣€ゅ鎰箾閸欏澧甸柟顔兼健瀹曞爼顢栭敃鈧ú鈺呭Φ閹版澘绠抽柟鎹愭硾楠炴劙鏌ｆ惔鈥冲辅闁稿鎹囬幃妤呮晲鎼粹€愁潾閻炴熬绠戦埞鎴︽偐閹颁礁鏅遍梺鍝ュУ鐢€崇暦閹达箑绠荤紓浣骨氶幏娲煟鎼粹剝璐″┑顔炬暬婵℃挳宕橀鐣屽幘闁诲骸婀辨慨鎾偂閹扮増鍊甸梻鍫熺◤閸嬨垻鈧娲栭悥濂搞€佸Δ浣瑰闁告瑥顦鍦磽閸屾艾鈧绮堟笟鈧、鏍幢濞戣鲸鏅炲┑鐐叉閹稿摜澹曢崸妤冨彄闁搞儯鍔庨埊鏇㈡煟閹惧崬鍔﹂柡宀嬬節瀹曞爼鈥︾€ｅ吀閭柛鈹惧亾濡炪倖甯婇懗鍓佺不閻愮儤鐓忛柛鈩冩礈椤︼附銇勯锝囩疄闁硅櫕绮撳畷褰掝敃閿濆洤绀佸┑鐘垫暩婵即宕归悡搴樻灃婵炴垯鍨洪弲婵嬫煏閸繍妲归柛濠傜仛閵囧嫰骞樼捄鍝勫濠电偞鎸搁…鐑藉蓟閺囥垹閱囨繝闈涙祩濡偞绻濆▓鍨灈闁稿﹤娼″璇测槈閵忕姈鈺呮煏婵炲灝鍔氶柟顖滃仧缁辨挻绗熼崶褎鐝紓鍌氱С缁舵岸鐛崘銊㈡瀻鐎电増绻傚﹢閬嶅焵椤掑﹦绉甸柛瀣嚇瀵爼骞栨担鍏夋嫽婵炶揪缍€濞咃絿鏁☉銏＄厱闁哄啠鍋撻柣妤佹礋閳ワ箓宕惰閺嬪酣鏌熼悙顒佺稇闁逞屽墮閻栧ジ寮婚妸鈺佺睄闁搞儺鐏濋幘缁樼厱婵炲棗绻掔粻鎻捛庨崶褝韬い銏＄☉椤劍鎯旈敐鍛繝鐢靛У椤旀牠宕伴弽顓炵９闁秆勵殔缁?
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] public static void WriteVarInt(FastBufferWriter w,int value){ w.WriteVarUInt32((uint)((value<<1)^(value>>31))); } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static int ReadVarInt(FastBufferReader r){ uint raw=(uint)r.ReadVarUInt32(); return (int)(raw>>1)^-((int)raw&1); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] public static void WriteUInt(FastBufferWriter w,int value){ if(value<0) throw new ArgumentOutOfRangeException(nameof(value), \"unsigned varint can not encode negative int\"); w.WriteVarUInt32((uint)value); } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static int ReadUInt(FastBufferReader r){ return r.ReadVarUInt32(); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] public static void WriteVarLong(FastBufferWriter w,long value){ w.WriteVarUInt64((ulong)((value<<1)^(value>>63))); } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static long ReadVarLong(FastBufferReader r){ ulong raw=r.ReadVarUInt64(); return (long)((raw>>1) ^ (ulong)-(long)(raw & 1UL)); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] public static void WriteULong(FastBufferWriter w,long value){ if(value<0) throw new ArgumentOutOfRangeException(nameof(value), \"unsigned varlong can not encode negative long\"); w.WriteVarUInt64((ulong)value); } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static long ReadULong(FastBufferReader r){ ulong raw=r.ReadVarUInt64(); if(raw>(ulong)long.MaxValue) throw new InvalidDataException(\"unsigned varlong overflow\"); return (long)raw; }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteSize(FastBufferWriter w,int size){ if(size<0) throw new ArgumentOutOfRangeException(nameof(size), \"size can not be negative\"); WriteUInt(w,size);} \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static int ReadSize(FastBufferReader r)=>ReadUInt(r);\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteInt(FastBufferWriter w,int v)=>WriteVarInt(w,v);\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static int ReadInt(FastBufferReader r)=>ReadVarInt(r);\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteLong(FastBufferWriter w,long v)=>WriteVarLong(w,v);\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static long ReadLong(FastBufferReader r)=>ReadVarLong(r);\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteShort(FastBufferWriter w,short v)=>WriteVarInt(w,v);\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static short ReadShort(FastBufferReader r)=>(short)ReadVarInt(r);\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteByte(FastBufferWriter w,byte v)=>w.WriteByte(v);\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static byte ReadByte(FastBufferReader r)=>r.ReadByte();\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteBool(FastBufferWriter w,bool v)=>w.WriteByte((byte)(v?1:0));\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static bool ReadBool(FastBufferReader r)=>r.ReadByte()!=0;\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteChar(FastBufferWriter w,char v)=>WriteUInt(w,v);\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static char ReadChar(FastBufferReader r)=>(char)ReadUInt(r);\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteFloat(FastBufferWriter w,float v){ BinaryPrimitives.WriteInt32BigEndian(w.Reserve(4), BitConverter.SingleToInt32Bits(v)); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static float ReadFloat(FastBufferReader r){ return BitConverter.Int32BitsToSingle(BinaryPrimitives.ReadInt32BigEndian(r.ReadSlice(4))); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteDouble(FastBufferWriter w,double v){ BinaryPrimitives.WriteInt64BigEndian(w.Reserve(8), BitConverter.DoubleToInt64Bits(v)); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static double ReadDouble(FastBufferReader r){ return BitConverter.Int64BitsToDouble(BinaryPrimitives.ReadInt64BigEndian(r.ReadSlice(8))); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteFixedInt(FastBufferWriter w,int v){ BinaryPrimitives.WriteInt32BigEndian(w.Reserve(4), v); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static int ReadFixedInt(FastBufferReader r){ return BinaryPrimitives.ReadInt32BigEndian(r.ReadSlice(4)); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteFixedLong(FastBufferWriter w,long v){ BinaryPrimitives.WriteInt64BigEndian(w.Reserve(8), v); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static long ReadFixedLong(FastBufferReader r){ return BinaryPrimitives.ReadInt64BigEndian(r.ReadSlice(8)); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteFixedShort(FastBufferWriter w,short v){ BinaryPrimitives.WriteInt16BigEndian(w.Reserve(2), v); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static short ReadFixedShort(FastBufferReader r){ return BinaryPrimitives.ReadInt16BigEndian(r.ReadSlice(2)); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteFixedChar(FastBufferWriter w,char v){ BinaryPrimitives.WriteUInt16BigEndian(w.Reserve(2), v); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static char ReadFixedChar(FastBufferReader r){ return (char)BinaryPrimitives.ReadUInt16BigEndian(r.ReadSlice(2)); }\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] public static void WriteString(FastBufferWriter w,string s){ string value=s??string.Empty; if(value.Length==0){ WriteSize(w,0); return; } byte[] cached=GetCachedUtf8Bytes(value); if(cached.Length>0){ WriteSize(w,cached.Length); w.WriteBytes(cached); return; } int n=Encoding.UTF8.GetByteCount(value); WriteSize(w,n); if(n==0) return; Span<byte> target=w.Reserve(n); Encoding.UTF8.GetBytes(value,target); } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] public static string ReadString(FastBufferReader r){ int n=ReadSize(r); if(n==0) return string.Empty; ReadOnlySpan<byte> utf8=r.ReadSlice(n); if(TryGetCachedAsciiString(utf8,out uint hash,out string? cached) && cached!=null) return cached; string value=Encoding.UTF8.GetString(utf8); if(hash!=0) CacheAsciiString(hash,value); return value; } \n");
            sb.append("public static void WriteCollection<T>(FastBufferWriter w, ICollection<T>? values, Action<FastBufferWriter,T> wr){ int count=values==null?0:values.Count; WriteSize(w, count); if(count==0) return; if(values is List<T> list){ var span=CollectionsMarshal.AsSpan(list); for(int i=0;i<span.Length;i++) wr(w,span[i]); return; } if(values is T[] arr){ for(int i=0;i<arr.Length;i++) wr(w,arr[i]); return; } if(values is IList<T> ilist){ for(int i=0;i<count;i++) wr(w,ilist[i]); return; } foreach(var x in values){ if(x is null) throw new InvalidDataException(\"collection item can not be null\"); wr(w,x); }} \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static C ReadCollection<T,C>(FastBufferReader r, Func<int,C> creator, Func<FastBufferReader,T> rd) where T : notnull where C : ICollection<T>{ int n=ReadSize(r); var c=creator(n) ?? throw new InvalidDataException(\"collection creator can not return null\"); for(int i=0;i<n;i++){ c.Add(rd(r)); } return c;} \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WritePresenceBits(FastBufferWriter w, ulong bits, int fieldCount){ int byteCount=(fieldCount+7)>>3; for(int i=0;i<byteCount;i++) w.WriteByte((byte)(bits >> (i<<3))); } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static ulong ReadPresenceBits(FastBufferReader r, int fieldCount){ ulong bits=0; int byteCount=(fieldCount+7)>>3; for(int i=0;i<byteCount;i++) bits|=(ulong)r.ReadByte() << (i<<3); return bits; } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WritePresenceBits(FastBufferWriter w, ulong[] words, int fieldCount){ int byteCount=(fieldCount+7)>>3; for(int i=0;i<byteCount;i++) w.WriteByte((byte)(words[i>>3] >> ((i & 7)<<3))); } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static ulong[] ReadPresenceWords(FastBufferReader r, int fieldCount){ int byteCount=(fieldCount+7)>>3; var words=new ulong[(fieldCount+63)>>6]; for(int i=0;i<byteCount;i++) words[i>>3]|=(ulong)r.ReadByte() << ((i & 7)<<3); return words; } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteOptional<T>(FastBufferWriter w, T? value, Action<FastBufferWriter,T> wr) where T : struct { bool hasValue=value.HasValue; WriteBool(w, hasValue); if(hasValue) wr(w,value.GetValueOrDefault());} public static void WriteOptional<T>(FastBufferWriter w, T? value, Action<FastBufferWriter,T> wr) where T : class { bool hasValue=value!=null; WriteBool(w, hasValue); if(hasValue) wr(w,value!);} [MethodImpl(MethodImplOptions.AggressiveInlining)] public static T? ReadOptionalValue<T>(FastBufferReader r, Func<FastBufferReader,T> rd) where T : struct { return ReadBool(r)? rd(r) : (T?)null; } [MethodImpl(MethodImplOptions.AggressiveInlining)] public static T? ReadOptionalRef<T>(FastBufferReader r, Func<FastBufferReader,T> rd) where T : class { return ReadBool(r)? rd(r) : null; } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteList<T>(FastBufferWriter w, ICollection<T> list, Action<FastBufferWriter,T> wr){ WriteCollection(w, list, wr);} \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static List<T> ReadList<T>(FastBufferReader r, Func<FastBufferReader,T> rd) where T : notnull { return ReadCollection(r, n=>BorrowList<T>(n), rd);} \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteSet<T>(FastBufferWriter w, ICollection<T> set, Action<FastBufferWriter,T> wr){ WriteCollection(w, set, wr);} \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static HashSet<T> ReadSet<T>(FastBufferReader r, Func<FastBufferReader,T> rd) where T : notnull { return ReadCollection(r, n=>BorrowHashSet<T>(n), rd);} \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteMap<K,V>(FastBufferWriter w, IDictionary<K,V>? map, Action<FastBufferWriter,K> wk, Action<FastBufferWriter,V> wv) where K : notnull where V : notnull { int count=map==null?0:map.Count; WriteSize(w, count); if(count==0) return; if(map is Dictionary<K,V> dict){ foreach(var e in dict){ wk(w,e.Key); wv(w,e.Value); } return; } foreach(var e in map){ wk(w,e.Key); wv(w,e.Value); } } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static Dictionary<K,V> ReadMap<K,V>(FastBufferReader r, Func<FastBufferReader,K> rk, Func<FastBufferReader,V> rv) where K : notnull where V : notnull { int n=ReadSize(r); var d=BorrowDictionary<K,V>(n); for(int i=0;i<n;i++){ d.Add(rk(r), rv(r)); } return d; } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static List<int> ReadPackedIntList(FastBufferReader r){ return ReadPackedIntList(r, null); } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static List<int> ReadPackedIntList(FastBufferReader r, List<int>? reuse){ int n=ReadSize(r); var list=reuse ?? BorrowList<int>(n); list.Clear(); if(list.Capacity<n) list.Capacity=n; for(int i=0;i<n;i++){ list.Add(ReadFixedInt(r)); } return list; } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WritePackedIntList(FastBufferWriter w, ICollection<int>? list){ int count=list==null?0:list.Count; WriteSize(w,count); if(count==0) return; if(list is List<int> typed){ var span=CollectionsMarshal.AsSpan(typed); for(int i=0;i<span.Length;i++) WriteFixedInt(w, span[i]); return; } foreach(var value in list){ WriteFixedInt(w, value); } } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static List<long> ReadPackedLongList(FastBufferReader r){ return ReadPackedLongList(r, null); } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static List<long> ReadPackedLongList(FastBufferReader r, List<long>? reuse){ int n=ReadSize(r); var list=reuse ?? BorrowList<long>(n); list.Clear(); if(list.Capacity<n) list.Capacity=n; for(int i=0;i<n;i++){ list.Add(ReadFixedLong(r)); } return list; } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WritePackedLongList(FastBufferWriter w, ICollection<long>? list){ int count=list==null?0:list.Count; WriteSize(w,count); if(count==0) return; if(list is List<long> typed){ var span=CollectionsMarshal.AsSpan(typed); for(int i=0;i<span.Length;i++) WriteFixedLong(w, span[i]); return; } foreach(var value in list){ WriteFixedLong(w, value); } } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static Dictionary<int,int> ReadPackedIntIntMap(FastBufferReader r){ return ReadPackedIntIntMap(r, null); } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static Dictionary<int,int> ReadPackedIntIntMap(FastBufferReader r, Dictionary<int,int>? reuse){ int n=ReadSize(r); var map=reuse ?? BorrowDictionary<int,int>(n); map.Clear(); map.EnsureCapacity(n); for(int i=0;i<n;i++){ map.Add(ReadFixedInt(r), ReadFixedInt(r)); } return map; } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WritePackedIntIntMap(FastBufferWriter w, IDictionary<int,int>? map){ int count=map==null?0:map.Count; WriteSize(w,count); if(count==0) return; foreach(var entry in map){ WriteFixedInt(w, entry.Key); WriteFixedInt(w, entry.Value); } } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static Dictionary<int,long> ReadPackedIntLongMap(FastBufferReader r){ return ReadPackedIntLongMap(r, null); } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static Dictionary<int,long> ReadPackedIntLongMap(FastBufferReader r, Dictionary<int,long>? reuse){ int n=ReadSize(r); var map=reuse ?? BorrowDictionary<int,long>(n); map.Clear(); map.EnsureCapacity(n); for(int i=0;i<n;i++){ map.Add(ReadFixedInt(r), ReadFixedLong(r)); } return map; } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WritePackedIntLongMap(FastBufferWriter w, IDictionary<int,long>? map){ int count=map==null?0:map.Count; WriteSize(w,count); if(count==0) return; foreach(var entry in map){ WriteFixedInt(w, entry.Key); WriteFixedLong(w, entry.Value); } } \n");
            sb.append("// Batched primitive array helpers.\n");
            sb.append("public static void WriteBytes(FastBufferWriter w, byte[]? a){ WriteSize(w, a==null?0:a.Length); if(a!=null && a.Length>0) w.WriteBytes(a);} public static byte[] ReadBytes(FastBufferReader r){ return ReadBytesInto(r, null); } public static byte[] ReadBytesInto(FastBufferReader r, byte[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<byte>(); var a=reuse!=null && reuse.Length==n ? reuse : GC.AllocateUninitializedArray<byte>(n); r.ReadBytes(a); return a;} \n");
            sb.append("public static void WriteIntArray(FastBufferWriter w,int[]? a){ WriteSize(w,a==null?0:a.Length); if(a!=null) foreach(var v in a) WriteInt(w,v);} public static int[] ReadIntArray(FastBufferReader r){ return ReadIntArrayInto(r, null); } public static int[] ReadIntArrayInto(FastBufferReader r, int[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<int>(); var a=reuse!=null && reuse.Length==n ? reuse : GC.AllocateUninitializedArray<int>(n); for(int i=0;i<n;i++) a[i]=ReadInt(r); return a; } \n");
            sb.append("public static void WriteLongArray(FastBufferWriter w,long[]? a){ WriteSize(w,a==null?0:a.Length); if(a!=null) foreach(var v in a) WriteLong(w,v);} public static long[] ReadLongArray(FastBufferReader r){ return ReadLongArrayInto(r, null); } public static long[] ReadLongArrayInto(FastBufferReader r, long[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<long>(); var a=reuse!=null && reuse.Length==n ? reuse : GC.AllocateUninitializedArray<long>(n); for(int i=0;i<n;i++) a[i]=ReadLong(r); return a; } \n");
            sb.append("public static void WriteShortArray(FastBufferWriter w,short[]? a){ WriteSize(w,a==null?0:a.Length); if(a!=null) foreach(var v in a) WriteShort(w,v);} public static short[] ReadShortArray(FastBufferReader r){ return ReadShortArrayInto(r, null); } public static short[] ReadShortArrayInto(FastBufferReader r, short[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<short>(); var a=reuse!=null && reuse.Length==n ? reuse : GC.AllocateUninitializedArray<short>(n); for(int i=0;i<n;i++) a[i]=ReadShort(r); return a; } \n");
            sb.append("public static void WriteBoolArray(FastBufferWriter w,bool[]? a){ WriteSize(w,a==null?0:a.Length); if(a!=null) foreach(var v in a) WriteBool(w,v);} public static bool[] ReadBoolArray(FastBufferReader r){ return ReadBoolArrayInto(r, null); } public static bool[] ReadBoolArrayInto(FastBufferReader r, bool[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<bool>(); var a=reuse!=null && reuse.Length==n ? reuse : GC.AllocateUninitializedArray<bool>(n); for(int i=0;i<n;i++) a[i]=ReadBool(r); return a; } \n");
            sb.append("public static void WriteCharArray(FastBufferWriter w,char[]? a){ WriteSize(w,a==null?0:a.Length); if(a!=null) foreach(var v in a) WriteChar(w,v);} public static char[] ReadCharArray(FastBufferReader r){ return ReadCharArrayInto(r, null); } public static char[] ReadCharArrayInto(FastBufferReader r, char[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<char>(); var a=reuse!=null && reuse.Length==n ? reuse : GC.AllocateUninitializedArray<char>(n); for(int i=0;i<n;i++) a[i]=ReadChar(r); return a; } \n");
            sb.append("public static void WriteFloatArray(FastBufferWriter w,float[]? a){ WriteSize(w,a==null?0:a.Length); if(a!=null) foreach(var v in a) WriteFloat(w,v);} public static float[] ReadFloatArray(FastBufferReader r){ return ReadFloatArrayInto(r, null); } public static float[] ReadFloatArrayInto(FastBufferReader r, float[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<float>(); var a=reuse!=null && reuse.Length==n ? reuse : GC.AllocateUninitializedArray<float>(n); for(int i=0;i<n;i++) a[i]=ReadFloat(r); return a; } \n");
            sb.append("public static void WriteDoubleArray(FastBufferWriter w,double[]? a){ WriteSize(w,a==null?0:a.Length); if(a!=null) foreach(var v in a) WriteDouble(w,v);} public static double[] ReadDoubleArray(FastBufferReader r){ return ReadDoubleArrayInto(r, null); } public static double[] ReadDoubleArrayInto(FastBufferReader r, double[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<double>(); var a=reuse!=null && reuse.Length==n ? reuse : GC.AllocateUninitializedArray<double>(n); for(int i=0;i<n;i++) a[i]=ReadDouble(r); return a; } \n");
            sb.append("public static void WriteFixedIntArray(FastBufferWriter w,int[]? a){ int count=a==null?0:a.Length; WriteSize(w,count); if(count==0||a==null) return; Span<byte> target=w.Reserve(count*4); if(!BitConverter.IsLittleEndian){ MemoryMarshal.AsBytes(a.AsSpan()).CopyTo(target); } else { for(int i=0,offset=0;i<count;i++,offset+=4) BinaryPrimitives.WriteInt32BigEndian(target.Slice(offset,4), a[i]); } } public static int[] ReadFixedIntArray(FastBufferReader r){ int n=ReadSize(r); if(n==0) return Array.Empty<int>(); var a=GC.AllocateUninitializedArray<int>(n); ReadOnlySpan<byte> source=r.ReadSlice(n*4); if(!BitConverter.IsLittleEndian){ source.CopyTo(MemoryMarshal.AsBytes(a.AsSpan())); } else { for(int i=0,offset=0;i<n;i++,offset+=4) a[i]=BinaryPrimitives.ReadInt32BigEndian(source.Slice(offset,4)); } return a; } \n");
            sb.append("public static void WriteFixedLongArray(FastBufferWriter w,long[]? a){ int count=a==null?0:a.Length; WriteSize(w,count); if(count==0||a==null) return; Span<byte> target=w.Reserve(count*8); if(!BitConverter.IsLittleEndian){ MemoryMarshal.AsBytes(a.AsSpan()).CopyTo(target); } else { for(int i=0,offset=0;i<count;i++,offset+=8) BinaryPrimitives.WriteInt64BigEndian(target.Slice(offset,8), a[i]); } } public static long[] ReadFixedLongArray(FastBufferReader r){ int n=ReadSize(r); if(n==0) return Array.Empty<long>(); var a=GC.AllocateUninitializedArray<long>(n); ReadOnlySpan<byte> source=r.ReadSlice(n*8); if(!BitConverter.IsLittleEndian){ source.CopyTo(MemoryMarshal.AsBytes(a.AsSpan())); } else { for(int i=0,offset=0;i<n;i++,offset+=8) a[i]=BinaryPrimitives.ReadInt64BigEndian(source.Slice(offset,8)); } return a; } \n");
            sb.append("public static void WriteFixedShortArray(FastBufferWriter w,short[]? a){ int count=a==null?0:a.Length; WriteSize(w,count); if(count==0||a==null) return; for(int i=0;i<count;i++) WriteFixedShort(w,a[i]); } public static short[] ReadFixedShortArray(FastBufferReader r){ int n=ReadSize(r); if(n==0) return Array.Empty<short>(); var a=GC.AllocateUninitializedArray<short>(n); for(int i=0;i<n;i++) a[i]=ReadFixedShort(r); return a; } \n");
            sb.append("public static void WriteFixedCharArray(FastBufferWriter w,char[]? a){ int count=a==null?0:a.Length; WriteSize(w,count); if(count==0||a==null) return; for(int i=0;i<count;i++) WriteFixedChar(w,a[i]); } public static char[] ReadFixedCharArray(FastBufferReader r){ int n=ReadSize(r); if(n==0) return Array.Empty<char>(); var a=GC.AllocateUninitializedArray<char>(n); for(int i=0;i<n;i++) a[i]=ReadFixedChar(r); return a; } \n");
            sb.append("public static void WriteFixedFloatArray(FastBufferWriter w,float[]? a){ int count=a==null?0:a.Length; WriteSize(w,count); if(count==0||a==null) return; Span<byte> target=w.Reserve(count*4); for(int i=0,offset=0;i<count;i++,offset+=4) BinaryPrimitives.WriteInt32BigEndian(target.Slice(offset,4), BitConverter.SingleToInt32Bits(a[i])); } public static float[] ReadFixedFloatArray(FastBufferReader r){ int n=ReadSize(r); if(n==0) return Array.Empty<float>(); var a=GC.AllocateUninitializedArray<float>(n); ReadOnlySpan<byte> source=r.ReadSlice(n*4); for(int i=0,offset=0;i<n;i++,offset+=4) a[i]=BitConverter.Int32BitsToSingle(BinaryPrimitives.ReadInt32BigEndian(source.Slice(offset,4))); return a; } \n");
            sb.append("public static void WriteFixedDoubleArray(FastBufferWriter w,double[]? a){ int count=a==null?0:a.Length; WriteSize(w,count); if(count==0||a==null) return; Span<byte> target=w.Reserve(count*8); for(int i=0,offset=0;i<count;i++,offset+=8) BinaryPrimitives.WriteInt64BigEndian(target.Slice(offset,8), BitConverter.DoubleToInt64Bits(a[i])); } public static double[] ReadFixedDoubleArray(FastBufferReader r){ int n=ReadSize(r); if(n==0) return Array.Empty<double>(); var a=GC.AllocateUninitializedArray<double>(n); ReadOnlySpan<byte> source=r.ReadSlice(n*8); for(int i=0,offset=0;i<n;i++,offset+=8) a[i]=BitConverter.Int64BitsToDouble(BinaryPrimitives.ReadInt64BigEndian(source.Slice(offset,8))); return a; } \n");
            sb.append("public static void WriteObjectArray<T>(FastBufferWriter w, T[]? a, Action<FastBufferWriter,T> wr) where T : notnull { int count=a==null?0:a.Length; WriteSize(w,count); if(count==0 || a==null) return; for(int i=0;i<count;i++){ wr(w,a[i]); } } public static T[] ReadObjectArray<T>(FastBufferReader r, Func<FastBufferReader,T> rd) where T : notnull { int n=ReadSize(r); var a=new T[n]; for(int i=0;i<n;i++){ a[i]=rd(r); } return a; } \n");
            // 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸绾惧綊鏌熼梻瀵割槮缁炬儳缍婇弻鐔兼⒒鐎靛壊妲紒鎯у⒔閹虫捇鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥秮閺岋綁顢橀悙鎼闂傚洤顦甸弻銊モ攽閸℃瑥顤€濡炪倕绻掓慨椋庢閹烘鐒垫い鎺嶈兌缁♀偓闂佺鏈〃鍡涘棘閳ь剟姊绘担铏瑰笡閽冭京鎲搁弶鍨殲缂佸倸绉归幃娆撴倻濡厧骞楅梻浣告贡閸庛倕顫忛悷鎵虫瀺闁哄洢鍨洪悡娆撴煕濞嗗浚妲洪柟钘夊暟缁辨帡鎮╁畷鍥ㄥ垱閻庢鍠楅幐鎶藉箖閵堝棙濯撮柛锔诲幘閳笺儵姊婚崒姘偓鐑芥嚄閸撲礁鍨濇い鏍仜缁€澶愭煛閸ャ儱鐒洪柡浣割儐閵囧嫰骞樼捄鐩掋垹鈹戦鍛⒋闁哄苯绉烽¨渚€鏌涢幘璺烘瀻妞ゎ偄绻愮叅妞ゅ繐瀚鎰版⒑缂佹ê濮堢憸鏉垮暞娣囧﹨顦规慨濠冩そ楠炴牠鎮欓幓鎺懶ら梻浣虹帛閹告悂藝閸楃倣锝夊箛闁附鏅㈤梺鍛婃处閸忔﹢骞忔繝姘拺缂佸瀵у﹢浼存煟閻旀繂娲ょ粈澶屸偓骞垮劚椤︿即鎮￠弴銏犵閺夊牆澧界壕鍧楁煥濞戞效闁哄瞼鍠栭悰顕€宕归鍙ユ偅闂佸墽绮悧鐘诲蓟濞戞﹩鐓ラ柛鎰╁妽濞堝墎绱撴担鍓插剰缂佺粯鍨归幑銏犫攽鐎ｎ亶娼婇梺鎸庣箓濡盯濡撮幇顓犵瘈闁靛骏绲剧涵鐐箾瀹割喖骞栭柣锝囧厴楠炲鏁冮埀顒傜不婵犳碍鐓欏Λ棰佽兌閸斿秹鏌涘澶嬫锭妞ゎ亜鍟存俊鍫曞幢濡儤娈梻浣侯焾椤戝棝鎯勯鐐靛祦婵°倕鎳忛崑銊╂煕濞戞☉鍫ュ箯濞差亝鐓熼柣妯哄级缁€宀勬煃瑜滈崜婵嗏枍閺囥垺鍊堕柛顐犲灮绾句粙鏌涚仦鐐殤鐎涙繈姊洪幐搴㈢８闁稿﹥绻堥獮鍐┿偅閸愨晛鈧鏌﹀Ο渚█闁哥偛鐖煎娲传閸曨剙绐涢梺绋款煭缂嶄線宕洪敓鐘插窛妞ゆ梹鍎崇敮鎯р攽閻橆喖鐏辨い鏇熺墵瀹曡瀵奸弶鎴犵枃閻庡厜鍋撻柛鏇ㄥ亞閿涙盯姊虹捄銊ユ灁濠殿喚鏁婚崺娑㈠箳濡や胶鍘遍柣蹇曞仜婢т粙骞婇崨顔轰簻闁挎棁鍋愰悾鐢告煛瀹€鈧崰鎾诲窗婵犲洤纭€闁绘劖婢橀弸鍫熶繆閵堝洤啸闁稿鐩弫鍐Ω瑜忔稉宥嗙箾閹寸們姘ｉ崼鐔剁箚妞ゆ牗绻傛禍褰掓煛閸♀晛浜版慨濠勭帛閹峰懏绗熼婊冨Ъ闂備焦妞块崜娆撳Χ缁嬭法鏆﹂柟瀛樼妇濡插牓鏌曡箛濞惧亾瀹曞洤绲介梻鍌欒兌缁垶寮婚妸鈺佽Е閻庯綆鍠楅崑鍌炴煟閺傚灝鎮戦柣鎾寸洴閺屾盯骞囬埡浣割瀳闂侀€炲苯澧柟鑺ョ矒瀵偊顢氶埀顒€鐣峰鍕闁惧繒娅㈢槐鏌ユ⒒娴ｈ櫣甯涢柨姘舵煕閻旈浠㈤棁澶嬨亜閺囨浜鹃梺鍝勫閳ь剙纾弳鍡涙煃瑜滈崜鐔风暦娴兼潙鍐€妞ゆ挾鍋熼悾鍝勨攽閻樿宸ラ柛姘耿閹垽宕楅崗鐓庡姃闂傚倷绶￠崑鍛矙閹烘嚦鐔哥節閸愵亞鐦堥梺姹囧灲濞佳勭墡婵＄偑鍊栧褰掓偋閻樿尙鏆﹀ù鍏兼綑閸愨偓濡炪倖鎸鹃崰鎾诲储閹扮増鈷戦柟绋挎捣缁犳挻銇勯敂璇茬仯缂?
            sb.append("// Batched primitive array helpers.\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteIntBatch(FastBufferWriter w,int[] values,int offset,int count){ WriteSize(w,count); if(count<=0) return; if(UseUnsafe && offset>=0 && offset+count<=values.Length){ unsafe{ fixed(int* p=&values[offset]){ int byteLen=count*4; var span=w.Reserve(byteLen); new ReadOnlySpan<byte>(p,byteLen).CopyTo(span); } } } else { for(int i=0;i<count;i++) WriteInt(w,values[offset+i]); } } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void ReadIntBatch(FastBufferReader r,int[] values,int offset,int count){ int n=ReadSize(r); if(n<=0) return; n=Math.Min(n,count); if(UseUnsafe && offset>=0 && offset+n<=values.Length){ unsafe{ fixed(int* p=&values[offset]){ r.ReadBytes(new Span<byte>(p,n*4)); } } } else { for(int i=0;i<n;i++) values[offset+i]=ReadInt(r); } } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteLongBatch(FastBufferWriter w,long[] values,int offset,int count){ WriteSize(w,count); if(count<=0) return; if(UseUnsafe && offset>=0 && offset+count<=values.Length){ unsafe{ fixed(long* p=&values[offset]){ int byteLen=count*8; var span=w.Reserve(byteLen); new ReadOnlySpan<byte>(p,byteLen).CopyTo(span); } } } else { for(int i=0;i<count;i++) WriteLong(w,values[offset+i]); } } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void ReadLongBatch(FastBufferReader r,long[] values,int offset,int count){ int n=ReadSize(r); if(n<=0) return; n=Math.Min(n,count); if(UseUnsafe && offset>=0 && offset+n<=values.Length){ unsafe{ fixed(long* p=&values[offset]){ r.ReadBytes(new Span<byte>(p,n*8)); } } } else { for(int i=0;i<n;i++) values[offset+i]=ReadLong(r); } } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteFloatBatch(FastBufferWriter w,float[] values,int offset,int count){ WriteSize(w,count); if(count<=0) return; if(UseUnsafe && offset>=0 && offset+count<=values.Length){ unsafe{ fixed(float* p=&values[offset]){ int byteLen=count*4; var span=w.Reserve(byteLen); new ReadOnlySpan<byte>(p,byteLen).CopyTo(span); } } } else { for(int i=0;i<count;i++) WriteFloat(w,values[offset+i]); } } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void ReadFloatBatch(FastBufferReader r,float[] values,int offset,int count){ int n=ReadSize(r); if(n<=0) return; n=Math.Min(n,count); if(UseUnsafe && offset>=0 && offset+n<=values.Length){ unsafe{ fixed(float* p=&values[offset]){ r.ReadBytes(new Span<byte>(p,n*4)); } } } else { for(int i=0;i<n;i++) values[offset+i]=ReadFloat(r); } } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void WriteDoubleBatch(FastBufferWriter w,double[] values,int offset,int count){ WriteSize(w,count); if(count<=0) return; if(UseUnsafe && offset>=0 && offset+count<=values.Length){ unsafe{ fixed(double* p=&values[offset]){ int byteLen=count*8; var span=w.Reserve(byteLen); new ReadOnlySpan<byte>(p,byteLen).CopyTo(span); } } } else { for(int i=0;i<count;i++) WriteDouble(w,values[offset+i]); } } \n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static void ReadDoubleBatch(FastBufferReader r,double[] values,int offset,int count){ int n=ReadSize(r); if(n<=0) return; n=Math.Min(n,count); if(UseUnsafe && offset>=0 && offset+n<=values.Length){ unsafe{ fixed(double* p=&values[offset]){ r.ReadBytes(new Span<byte>(p,n*8)); } } } else { for(int i=0;i<n;i++) values[offset+i]=ReadDouble(r); } } \n");
            sb.append("public static void WriteVarInt(BinaryWriter w,int value){ uint v=(uint)((value<<1)^(value>>31)); while((v & ~0x7Fu)!=0){ w.Write((byte)((v & 0x7F)|0x80)); v >>=7;} w.Write((byte)v);} \n");
            sb.append("public static int ReadVarInt(BinaryReader r){ int num=0; int result=0; byte b; do{ b=r.ReadByte(); int val=b & 0x7F; result |= (val << (7*num)); num++; }while((b & 0x80)!=0); int tmp=(int)((uint)result>>1) ^ -(result & 1); return tmp; }\n");
            sb.append("public static void WriteUInt(BinaryWriter w,int value){ if(value<0) throw new ArgumentOutOfRangeException(nameof(value), \"unsigned varint can not encode negative int\"); uint v=(uint)value; while((v & ~0x7Fu)!=0){ w.Write((byte)((v & 0x7F)|0x80)); v >>=7;} w.Write((byte)v);} \n");
            sb.append("public static int ReadUInt(BinaryReader r){ int num=0; int result=0; byte b; do{ b=r.ReadByte(); int val=b & 0x7F; result |= (val << (7*num)); num++; if(num>5) throw new InvalidDataException(\"unsigned varint is too long\"); }while((b & 0x80)!=0); return result; }\n");
            sb.append("public static void WriteVarLong(BinaryWriter w,long value){ ulong v=(ulong)((value<<1)^(value>>63)); while((v & ~0x7FUL)!=0){ w.Write((byte)((v & 0x7F)|0x80)); v >>=7;} w.Write((byte)v);} \n");
            sb.append("public static long ReadVarLong(BinaryReader r){ int num=0; long result=0; byte b; do{ b=r.ReadByte(); long val=b & 0x7F; result |= (val << (7*num)); num++; }while((b & 0x80)!=0); long tmp=(long)(((ulong)result>>1) ^ (ulong)-(long)(result & 1)); return (long)tmp; }\n");
            sb.append("public static void WriteULong(BinaryWriter w,long value){ if(value<0) throw new ArgumentOutOfRangeException(nameof(value), \"unsigned varlong can not encode negative long\"); ulong v=(ulong)value; while((v & ~0x7FUL)!=0){ w.Write((byte)((v & 0x7F)|0x80)); v >>=7;} w.Write((byte)v);} \n");
            sb.append("public static long ReadULong(BinaryReader r){ int num=0; long result=0; byte b; do{ b=r.ReadByte(); long val=b & 0x7F; result |= (val << (7*num)); num++; if(num>10) throw new InvalidDataException(\"unsigned varlong is too long\"); }while((b & 0x80)!=0); return result; }\n");
            sb.append("private static void ReadExactly(BinaryReader r, Span<byte> dest){ int read=0; while(read<dest.Length){ int n=r.Read(dest.Slice(read)); if(n<=0) throw new EndOfStreamException(); read+=n; } }\n");
            sb.append("private static void ReadExactly(BinaryReader r, byte[] dest){ int read=0; while(read<dest.Length){ int n=r.Read(dest, read, dest.Length-read); if(n<=0) throw new EndOfStreamException(); read+=n; } }\n");
            sb.append("public static void WriteSize(BinaryWriter w,int size){ if(size<0) throw new ArgumentOutOfRangeException(nameof(size), \"size can not be negative\"); WriteUInt(w,size);} public static int ReadSize(BinaryReader r)=>ReadUInt(r);\n");
            sb.append("public static void WriteInt(BinaryWriter w,int v)=>WriteVarInt(w,v); public static int ReadInt(BinaryReader r)=>ReadVarInt(r);\n");
            sb.append("public static void WriteLong(BinaryWriter w,long v)=>WriteVarLong(w,v); public static long ReadLong(BinaryReader r)=>ReadVarLong(r);\n");
            sb.append("public static void WriteShort(BinaryWriter w,short v)=>WriteVarInt(w,v); public static short ReadShort(BinaryReader r)=>(short)ReadVarInt(r);\n");
            sb.append("public static void WriteByte(BinaryWriter w,byte v)=>w.Write(v); public static byte ReadByte(BinaryReader r)=>r.ReadByte();\n");
            sb.append("public static void WriteBool(BinaryWriter w,bool v)=>w.Write(v); public static bool ReadBool(BinaryReader r)=>r.ReadBoolean();\n");
            sb.append("public static void WriteChar(BinaryWriter w,char v)=>WriteUInt(w,v); public static char ReadChar(BinaryReader r)=>(char)ReadUInt(r);\n");
            sb.append("public static void WriteFloat(BinaryWriter w,float v){ Span<byte> b=stackalloc byte[4]; BinaryPrimitives.WriteInt32BigEndian(b, BitConverter.SingleToInt32Bits(v)); w.Write(b);} public static float ReadFloat(BinaryReader r){ Span<byte> b=stackalloc byte[4]; ReadExactly(r, b); return BitConverter.Int32BitsToSingle(BinaryPrimitives.ReadInt32BigEndian(b)); }\n");
            sb.append("public static void WriteDouble(BinaryWriter w,double v){ Span<byte> b=stackalloc byte[8]; BinaryPrimitives.WriteInt64BigEndian(b, BitConverter.DoubleToInt64Bits(v)); w.Write(b);} public static double ReadDouble(BinaryReader r){ Span<byte> b=stackalloc byte[8]; ReadExactly(r, b); return BitConverter.Int64BitsToDouble(BinaryPrimitives.ReadInt64BigEndian(b)); }\n");
            sb.append("public static void WriteFixedInt(BinaryWriter w,int v){ Span<byte> b=stackalloc byte[4]; BinaryPrimitives.WriteInt32BigEndian(b, v); w.Write(b);} public static int ReadFixedInt(BinaryReader r){ Span<byte> b=stackalloc byte[4]; ReadExactly(r, b); return BinaryPrimitives.ReadInt32BigEndian(b); }\n");
            sb.append("public static void WriteFixedLong(BinaryWriter w,long v){ Span<byte> b=stackalloc byte[8]; BinaryPrimitives.WriteInt64BigEndian(b, v); w.Write(b);} public static long ReadFixedLong(BinaryReader r){ Span<byte> b=stackalloc byte[8]; ReadExactly(r, b); return BinaryPrimitives.ReadInt64BigEndian(b); }\n");
            sb.append("public static void WriteFixedShort(BinaryWriter w,short v){ Span<byte> b=stackalloc byte[2]; BinaryPrimitives.WriteInt16BigEndian(b, v); w.Write(b);} public static short ReadFixedShort(BinaryReader r){ Span<byte> b=stackalloc byte[2]; ReadExactly(r, b); return BinaryPrimitives.ReadInt16BigEndian(b); }\n");
            sb.append("public static void WriteFixedChar(BinaryWriter w,char v){ Span<byte> b=stackalloc byte[2]; BinaryPrimitives.WriteUInt16BigEndian(b, v); w.Write(b);} public static char ReadFixedChar(BinaryReader r){ Span<byte> b=stackalloc byte[2]; ReadExactly(r, b); return (char)BinaryPrimitives.ReadUInt16BigEndian(b); }\n");
            sb.append("public static void WriteString(BinaryWriter w,string s){ string value=s??string.Empty; int n=Encoding.UTF8.GetByteCount(value); WriteSize(w,n); if(n==0) return; Span<byte> b=n<=256? stackalloc byte[n] : new byte[n]; Encoding.UTF8.GetBytes(value, b); w.Write(b);} public static string ReadString(BinaryReader r){ int n=ReadSize(r); if(n==0) return string.Empty; Span<byte> b=n<=256? stackalloc byte[n] : new byte[n]; ReadExactly(r, b); return Encoding.UTF8.GetString(b);} \n");
            sb.append("public static void WriteCollection<T>(BinaryWriter w, ICollection<T>? values, Action<BinaryWriter,T> wr){ int count=values==null?0:values.Count; WriteSize(w, count); if(count==0) return; if(values is List<T> list){ var span=CollectionsMarshal.AsSpan(list); for(int i=0;i<span.Length;i++) wr(w,span[i]); return; } if(values is T[] arr){ for(int i=0;i<arr.Length;i++) wr(w,arr[i]); return; } if(values is IList<T> ilist){ for(int i=0;i<count;i++) wr(w,ilist[i]); return; } foreach(var x in values){ if(x is null) throw new InvalidDataException(\"collection item can not be null\"); wr(w,x); }} \n");
            sb.append("public static C ReadCollection<T,C>(BinaryReader r, Func<int,C> creator, Func<BinaryReader,T> rd) where T : notnull where C : ICollection<T>{ int n=ReadSize(r); var c=creator(n) ?? throw new InvalidDataException(\"collection creator can not return null\"); for(int i=0;i<n;i++){ c.Add(rd(r)); } return c;} \n");
            sb.append("public static void WritePresenceBits(BinaryWriter w, ulong bits, int fieldCount){ int byteCount=(fieldCount+7)>>3; for(int i=0;i<byteCount;i++) w.Write((byte)(bits >> (i<<3))); } public static ulong ReadPresenceBits(BinaryReader r, int fieldCount){ ulong bits=0; int byteCount=(fieldCount+7)>>3; for(int i=0;i<byteCount;i++) bits|=(ulong)r.ReadByte() << (i<<3); return bits; } \n");
            sb.append("public static void WritePresenceBits(BinaryWriter w, ulong[] words, int fieldCount){ int byteCount=(fieldCount+7)>>3; for(int i=0;i<byteCount;i++) w.Write((byte)(words[i>>3] >> ((i & 7)<<3))); } public static ulong[] ReadPresenceWords(BinaryReader r, int fieldCount){ int byteCount=(fieldCount+7)>>3; var words=new ulong[(fieldCount+63)>>6]; for(int i=0;i<byteCount;i++) words[i>>3]|=(ulong)r.ReadByte() << ((i & 7)<<3); return words; } public static bool IsPresenceBitSet(ulong[] words, int index){ return (words[index>>6] & (1UL << (index & 63))) != 0UL; } \n");
            sb.append("public static void WriteOptional<T>(BinaryWriter w, T? value, Action<BinaryWriter,T> wr) where T : struct { bool hasValue=value.HasValue; WriteBool(w, hasValue); if(hasValue) wr(w,value.GetValueOrDefault());} public static void WriteOptional<T>(BinaryWriter w, T? value, Action<BinaryWriter,T> wr) where T : class { bool hasValue=value!=null; WriteBool(w, hasValue); if(hasValue) wr(w,value!);} public static T? ReadOptionalValue<T>(BinaryReader r, Func<BinaryReader,T> rd) where T : struct { return ReadBool(r)? rd(r) : (T?)null; } public static T? ReadOptionalRef<T>(BinaryReader r, Func<BinaryReader,T> rd) where T : class { return ReadBool(r)? rd(r) : null; } \n");
            sb.append("public static void WriteList<T>(BinaryWriter w, ICollection<T> list, Action<BinaryWriter,T> wr){ WriteCollection(w, list, wr);} \n");
            sb.append("public static List<T> ReadList<T>(BinaryReader r, Func<BinaryReader,T> rd) where T : notnull { return ReadCollection(r, n=>BorrowList<T>(n), rd);} \n");
            sb.append("public static void WriteSet<T>(BinaryWriter w, ICollection<T> set, Action<BinaryWriter,T> wr){ WriteCollection(w, set, wr);} \n");
            sb.append("public static HashSet<T> ReadSet<T>(BinaryReader r, Func<BinaryReader,T> rd) where T : notnull { return ReadCollection(r, n=>BorrowHashSet<T>(n), rd);} \n");
            sb.append("public static void WriteMap<K,V>(BinaryWriter w, IDictionary<K,V>? map, Action<BinaryWriter,K> wk, Action<BinaryWriter,V> wv) where K : notnull where V : notnull { int count=map==null?0:map.Count; WriteSize(w, count); if(count==0) return; if(map is Dictionary<K,V> dict){ foreach(var e in dict){ wk(w,e.Key); wv(w,e.Value); } return; } foreach(var e in map){ wk(w,e.Key); wv(w,e.Value); } } \n");
            sb.append("public static Dictionary<K,V> ReadMap<K,V>(BinaryReader r, Func<BinaryReader,K> rk, Func<BinaryReader,V> rv) where K : notnull where V : notnull { int n=ReadSize(r); var d=BorrowDictionary<K,V>(n); for(int i=0;i<n;i++){ d.Add(rk(r), rv(r)); } return d; } \n");
            sb.append("public static List<int> ReadPackedIntList(BinaryReader r){ return ReadPackedIntList(r, null); } \n");
            sb.append("public static List<int> ReadPackedIntList(BinaryReader r, List<int>? reuse){ int n=ReadSize(r); var list=reuse ?? BorrowList<int>(n); list.Clear(); if(list.Capacity<n) list.Capacity=n; for(int i=0;i<n;i++){ list.Add(ReadFixedInt(r)); } return list; } \n");
            sb.append("public static void WritePackedIntList(BinaryWriter w, ICollection<int>? list){ int count=list==null?0:list.Count; WriteSize(w,count); if(count==0) return; if(list is List<int> typed){ var span=CollectionsMarshal.AsSpan(typed); for(int i=0;i<span.Length;i++) WriteFixedInt(w, span[i]); return; } foreach(var value in list){ WriteFixedInt(w, value); } } \n");
            sb.append("public static List<long> ReadPackedLongList(BinaryReader r){ return ReadPackedLongList(r, null); } \n");
            sb.append("public static List<long> ReadPackedLongList(BinaryReader r, List<long>? reuse){ int n=ReadSize(r); var list=reuse ?? BorrowList<long>(n); list.Clear(); if(list.Capacity<n) list.Capacity=n; for(int i=0;i<n;i++){ list.Add(ReadFixedLong(r)); } return list; } \n");
            sb.append("public static void WritePackedLongList(BinaryWriter w, ICollection<long>? list){ int count=list==null?0:list.Count; WriteSize(w,count); if(count==0) return; if(list is List<long> typed){ var span=CollectionsMarshal.AsSpan(typed); for(int i=0;i<span.Length;i++) WriteFixedLong(w, span[i]); return; } foreach(var value in list){ WriteFixedLong(w, value); } } \n");
            sb.append("public static Dictionary<int,int> ReadPackedIntIntMap(BinaryReader r){ return ReadPackedIntIntMap(r, null); } \n");
            sb.append("public static Dictionary<int,int> ReadPackedIntIntMap(BinaryReader r, Dictionary<int,int>? reuse){ int n=ReadSize(r); var map=reuse ?? BorrowDictionary<int,int>(n); map.Clear(); map.EnsureCapacity(n); for(int i=0;i<n;i++){ map.Add(ReadFixedInt(r), ReadFixedInt(r)); } return map; } \n");
            sb.append("public static void WritePackedIntIntMap(BinaryWriter w, IDictionary<int,int>? map){ int count=map==null?0:map.Count; WriteSize(w,count); if(count==0) return; foreach(var entry in map){ WriteFixedInt(w, entry.Key); WriteFixedInt(w, entry.Value); } } \n");
            sb.append("public static Dictionary<int,long> ReadPackedIntLongMap(BinaryReader r){ return ReadPackedIntLongMap(r, null); } \n");
            sb.append("public static Dictionary<int,long> ReadPackedIntLongMap(BinaryReader r, Dictionary<int,long>? reuse){ int n=ReadSize(r); var map=reuse ?? BorrowDictionary<int,long>(n); map.Clear(); map.EnsureCapacity(n); for(int i=0;i<n;i++){ map.Add(ReadFixedInt(r), ReadFixedLong(r)); } return map; } \n");
            sb.append("public static void WritePackedIntLongMap(BinaryWriter w, IDictionary<int,long>? map){ int count=map==null?0:map.Count; WriteSize(w,count); if(count==0) return; foreach(var entry in map){ WriteFixedInt(w, entry.Key); WriteFixedLong(w, entry.Value); } } \n");
            sb.append("// Batched primitive array helpers.\n");
            sb.append("public static void WriteBytes(BinaryWriter w, byte[]? a){ WriteSize(w, a==null?0:a.Length); if(a!=null && a.Length>0) w.Write(a);} public static byte[] ReadBytes(BinaryReader r){ return ReadBytesInto(r, null); } public static byte[] ReadBytesInto(BinaryReader r, byte[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<byte>(); var a=reuse!=null && reuse.Length==n ? reuse : new byte[n]; ReadExactly(r, a); return a;} \n");
            sb.append("public static void WriteIntArray(BinaryWriter w,int[]? a){ WriteSize(w,a==null?0:a.Length); if(a!=null) foreach(var v in a) WriteInt(w,v);} public static int[] ReadIntArray(BinaryReader r){ return ReadIntArrayInto(r, null); } public static int[] ReadIntArrayInto(BinaryReader r, int[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<int>(); var a=reuse!=null && reuse.Length==n ? reuse : new int[n]; for(int i=0;i<n;i++) a[i]=ReadInt(r); return a; } \n");
            sb.append("public static void WriteLongArray(BinaryWriter w,long[]? a){ WriteSize(w,a==null?0:a.Length); if(a!=null) foreach(var v in a) WriteLong(w,v);} public static long[] ReadLongArray(BinaryReader r){ return ReadLongArrayInto(r, null); } public static long[] ReadLongArrayInto(BinaryReader r, long[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<long>(); var a=reuse!=null && reuse.Length==n ? reuse : new long[n]; for(int i=0;i<n;i++) a[i]=ReadLong(r); return a; } \n");
            sb.append("public static void WriteShortArray(BinaryWriter w,short[]? a){ WriteSize(w,a==null?0:a.Length); if(a!=null) foreach(var v in a) WriteShort(w,v);} public static short[] ReadShortArray(BinaryReader r){ return ReadShortArrayInto(r, null); } public static short[] ReadShortArrayInto(BinaryReader r, short[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<short>(); var a=reuse!=null && reuse.Length==n ? reuse : new short[n]; for(int i=0;i<n;i++) a[i]=ReadShort(r); return a; } \n");
            sb.append("public static void WriteBoolArray(BinaryWriter w,bool[]? a){ WriteSize(w,a==null?0:a.Length); if(a!=null) foreach(var v in a) WriteBool(w,v);} public static bool[] ReadBoolArray(BinaryReader r){ return ReadBoolArrayInto(r, null); } public static bool[] ReadBoolArrayInto(BinaryReader r, bool[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<bool>(); var a=reuse!=null && reuse.Length==n ? reuse : new bool[n]; for(int i=0;i<n;i++) a[i]=ReadBool(r); return a; } \n");
            sb.append("public static void WriteCharArray(BinaryWriter w,char[]? a){ WriteSize(w,a==null?0:a.Length); if(a!=null) foreach(var v in a) WriteChar(w,v);} public static char[] ReadCharArray(BinaryReader r){ return ReadCharArrayInto(r, null); } public static char[] ReadCharArrayInto(BinaryReader r, char[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<char>(); var a=reuse!=null && reuse.Length==n ? reuse : new char[n]; for(int i=0;i<n;i++) a[i]=ReadChar(r); return a; } \n");
            sb.append("public static void WriteFloatArray(BinaryWriter w,float[]? a){ WriteSize(w,a==null?0:a.Length); if(a!=null) foreach(var v in a) WriteFloat(w,v);} public static float[] ReadFloatArray(BinaryReader r){ return ReadFloatArrayInto(r, null); } public static float[] ReadFloatArrayInto(BinaryReader r, float[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<float>(); var a=reuse!=null && reuse.Length==n ? reuse : new float[n]; for(int i=0;i<n;i++) a[i]=ReadFloat(r); return a; } \n");
            sb.append("public static void WriteDoubleArray(BinaryWriter w,double[]? a){ WriteSize(w,a==null?0:a.Length); if(a!=null) foreach(var v in a) WriteDouble(w,v);} public static double[] ReadDoubleArray(BinaryReader r){ return ReadDoubleArrayInto(r, null); } public static double[] ReadDoubleArrayInto(BinaryReader r, double[]? reuse){ int n=ReadSize(r); if(n==0) return Array.Empty<double>(); var a=reuse!=null && reuse.Length==n ? reuse : new double[n]; for(int i=0;i<n;i++) a[i]=ReadDouble(r); return a; } \n");
            sb.append("public static void WriteFixedIntArray(BinaryWriter w,int[]? a){ int count=a==null?0:a.Length; WriteSize(w,count); if(count==0||a==null) return; for(int i=0;i<count;i++) WriteFixedInt(w,a[i]); } public static int[] ReadFixedIntArray(BinaryReader r){ int n=ReadSize(r); if(n==0) return Array.Empty<int>(); var a=new int[n]; for(int i=0;i<n;i++) a[i]=ReadFixedInt(r); return a; } \n");
            sb.append("public static void WriteFixedLongArray(BinaryWriter w,long[]? a){ int count=a==null?0:a.Length; WriteSize(w,count); if(count==0||a==null) return; for(int i=0;i<count;i++) WriteFixedLong(w,a[i]); } public static long[] ReadFixedLongArray(BinaryReader r){ int n=ReadSize(r); if(n==0) return Array.Empty<long>(); var a=new long[n]; for(int i=0;i<n;i++) a[i]=ReadFixedLong(r); return a; } \n");
            sb.append("public static void WriteFixedShortArray(BinaryWriter w,short[]? a){ int count=a==null?0:a.Length; WriteSize(w,count); if(count==0||a==null) return; for(int i=0;i<count;i++) WriteFixedShort(w,a[i]); } public static short[] ReadFixedShortArray(BinaryReader r){ int n=ReadSize(r); if(n==0) return Array.Empty<short>(); var a=new short[n]; for(int i=0;i<n;i++) a[i]=ReadFixedShort(r); return a; } \n");
            sb.append("public static void WriteFixedCharArray(BinaryWriter w,char[]? a){ int count=a==null?0:a.Length; WriteSize(w,count); if(count==0||a==null) return; for(int i=0;i<count;i++) WriteFixedChar(w,a[i]); } public static char[] ReadFixedCharArray(BinaryReader r){ int n=ReadSize(r); if(n==0) return Array.Empty<char>(); var a=new char[n]; for(int i=0;i<n;i++) a[i]=ReadFixedChar(r); return a; } \n");
            sb.append("public static void WriteFixedFloatArray(BinaryWriter w,float[]? a){ int count=a==null?0:a.Length; WriteSize(w,count); if(count==0||a==null) return; for(int i=0;i<count;i++) WriteFloat(w,a[i]); } public static float[] ReadFixedFloatArray(BinaryReader r){ int n=ReadSize(r); if(n==0) return Array.Empty<float>(); var a=new float[n]; for(int i=0;i<n;i++) a[i]=ReadFloat(r); return a; } \n");
            sb.append("public static void WriteFixedDoubleArray(BinaryWriter w,double[]? a){ int count=a==null?0:a.Length; WriteSize(w,count); if(count==0||a==null) return; for(int i=0;i<count;i++) WriteDouble(w,a[i]); } public static double[] ReadFixedDoubleArray(BinaryReader r){ int n=ReadSize(r); if(n==0) return Array.Empty<double>(); var a=new double[n]; for(int i=0;i<n;i++) a[i]=ReadDouble(r); return a; } \n");
            sb.append("public static void WriteObjectArray<T>(BinaryWriter w, T[]? a, Action<BinaryWriter,T> wr) where T : notnull { int count=a==null?0:a.Length; WriteSize(w,count); if(count==0 || a==null) return; for(int i=0;i<count;i++){ wr(w,a[i]); } } public static T[] ReadObjectArray<T>(BinaryReader r, Func<BinaryReader,T> rd) where T : notnull { int n=ReadSize(r); var a=new T[n]; for(int i=0;i<n;i++){ a[i]=rd(r); } return a; } \n");
            sb.append("}}\n");
            return sb.toString();
        }
        static String csMapType(String t){
            if(t.equals("int")) return "int";
            if(t.equals("long")) return "long";
            if(t.equals("byte")) return "byte";
            if(t.equals("short")) return "short";
            if(t.equals("boolean")) return "bool";
            if(t.equals("char")) return "char";
            if(t.equals("float")) return "float";
            if(t.equals("double")) return "double";
            if(t.equals("Integer")) return "int";
            if(t.equals("Long")) return "long";
            if(t.equals("Byte")) return "byte";
            if(t.equals("Short")) return "short";
            if(t.equals("Boolean")) return "bool";
            if(t.equals("Character")) return "char";
            if(t.equals("Float")) return "float";
            if(t.equals("Double")) return "double";
            if(t.equals("String")) return "string";
            if(Codegen.isOptionalType(t)) return csNullableType(Codegen.genericBody(t).trim());
            if(t.endsWith("[]")) return csMapType(t.substring(0,t.length()-2))+"[]";
            if(Codegen.isListLikeType(t)){
                String inner=Codegen.genericBody(t).trim();
                if("LinkedList".equals(Codegen.canonicalContainerType(t))){
                    return "LinkedList<"+csMapType(inner)+">";
                }
                return "List<"+csMapType(inner)+">";
            }
            if(Codegen.isQueueLikeType(t)) return "List<"+csMapType(Codegen.genericBody(t).trim())+">";
            if(Codegen.isSetLikeType(t)) return "HashSet<"+csMapType(Codegen.genericBody(t).trim())+">";
            if(Codegen.isMapLikeType(t)){ 
                String inside=Codegen.genericBody(t);
                List<String> kv=Codegen.splitTopLevel(inside, ',');
                return "Dictionary<"+csMapType(kv.get(0).trim())+","+csMapType(kv.get(1).trim())+">";
            }
            return t;
        }
        static String csNullableType(String t){
            return csMapType(t)+"?";
        }
        static boolean isCsValueType(String t){
            return t.equals("int") || t.equals("long") || t.equals("byte") || t.equals("short")
                    || t.equals("boolean") || t.equals("char") || t.equals("float") || t.equals("double")
                    || t.equals("Integer") || t.equals("Long") || t.equals("Byte") || t.equals("Short")
                    || t.equals("Boolean") || t.equals("Character") || t.equals("Float") || t.equals("Double")
                    || Codegen.ENUMS.contains(t);
        }
        static String csWriteCacheType(String t){
            if(Codegen.isOptionalType(t) || isCsValueType(t)) return csMapType(t);
            return csMapType(t)+"?";
        }
        static String csPresenceValueVar(Field field){
            return Codegen.childVar(field.name, "cached");
        }
        static String csPresenceHasVar(Field field){
            return Codegen.childVar(field.name, "hasWireValue");
        }
        static String csStableWriteValueExpr(String var, String t){
            if(Codegen.isOptionalType(t)){
                String inner=Codegen.genericBody(t).trim();
                return isCsValueType(inner)? var+".GetValueOrDefault()" : var+"!";
            }
            return isCsValueType(t)? var : var+"!";
        }
        static boolean isCsReusableObjectType(String t){
            return !isCsValueType(t)
                    && !t.equals("String")
                    && !t.equals("string")
                    && !Codegen.isContainerType(t)
                    && !t.endsWith("[]");
        }
        static String csHasWireValueExpr(String var, String t){
            if(Codegen.isOptionalType(t)) return csOptionalHasValueExpr(var);
            if(t.equals("int") || t.equals("Integer")) return var+" != 0";
            if(t.equals("long") || t.equals("Long")) return var+" != 0L";
            if(t.equals("byte") || t.equals("Byte")) return var+" != (byte)0";
            if(t.equals("short") || t.equals("Short")) return var+" != (short)0";
            if(t.equals("boolean") || t.equals("Boolean")) return var;
            if(t.equals("char") || t.equals("Character")) return var+" != '\\0'";
            if(t.equals("float") || t.equals("Float")) return var+" != 0F";
            if(t.equals("double") || t.equals("Double")) return var+" != 0D";
            if(t.equals("String") || t.equals("string")) return "!string.IsNullOrEmpty("+var+")";
            if(Codegen.ENUMS.contains(t)) return "(int)"+var+" != 0";
            if(t.endsWith("[]")) return var+" != null && "+var+".Length != 0";
            if(Codegen.isListLikeType(t) || Codegen.isSetLikeType(t) || Codegen.isQueueLikeType(t) || Codegen.isMapLikeType(t)){
                return var+" != null && "+var+".Count != 0";
            }
            return "true";
        }
        static String csDefaultValueExpr(String t){
            if(Codegen.isOptionalType(t)) return "default";
            if(t.equals("int") || t.equals("Integer")) return "0";
            if(t.equals("long") || t.equals("Long")) return "0L";
            if(t.equals("byte") || t.equals("Byte")) return "(byte)0";
            if(t.equals("short") || t.equals("Short")) return "(short)0";
            if(t.equals("boolean") || t.equals("Boolean")) return "false";
            if(t.equals("char") || t.equals("Character")) return "'\\0'";
            if(t.equals("float") || t.equals("Float")) return "0F";
            if(t.equals("double") || t.equals("Double")) return "0D";
            if(t.equals("String") || t.equals("string")) return "string.Empty";
            if(Codegen.ENUMS.contains(t)) return "("+t+")0";
            if(t.endsWith("[]")){
                return "System.Array.Empty<"+csMapType(t.substring(0,t.length()-2))+">()";
            }
            if(Codegen.isListLikeType(t)){
                String inner=csMapType(Codegen.genericBody(t).trim());
                if("LinkedList".equals(Codegen.canonicalContainerType(t))){
                    return "new LinkedList<"+inner+">()";
                }
                return "BufUtil.BorrowList<"+inner+">(0)";
            }
            if(Codegen.isQueueLikeType(t)){
                return "BufUtil.BorrowList<"+csMapType(Codegen.genericBody(t).trim())+">(0)";
            }
            if(Codegen.isSetLikeType(t)){
                return "BufUtil.BorrowHashSet<"+csMapType(Codegen.genericBody(t).trim())+">(0)";
            }
            if(Codegen.isMapLikeType(t)){
                String inside=Codegen.genericBody(t);
                List<String> kv=Codegen.splitTopLevel(inside, ',');
                return "BufUtil.BorrowDictionary<"+csMapType(kv.get(0).trim())+","+csMapType(kv.get(1).trim())+">(0)";
            }
            return "default!";
        }
        static String csBorrowCollectionExpr(String t, String capacityExpr){
            if(Codegen.isListLikeType(t)){
                String inner=csMapType(Codegen.genericBody(t).trim());
                if("LinkedList".equals(Codegen.canonicalContainerType(t))){
                    return "new LinkedList<"+inner+">()";
                }
                return "BufUtil.BorrowList<"+inner+">("+capacityExpr+")";
            }
            if(Codegen.isQueueLikeType(t)){
                return "BufUtil.BorrowList<"+csMapType(Codegen.genericBody(t).trim())+">("+capacityExpr+")";
            }
            if(Codegen.isSetLikeType(t)){
                return "BufUtil.BorrowHashSet<"+csMapType(Codegen.genericBody(t).trim())+">("+capacityExpr+")";
            }
            if(Codegen.isMapLikeType(t)){
                String inside=Codegen.genericBody(t);
                List<String> kv=Codegen.splitTopLevel(inside, ',');
                return "BufUtil.BorrowDictionary<"+csMapType(kv.get(0).trim())+","+csMapType(kv.get(1).trim())+">("+capacityExpr+")";
            }
            return csDefaultValueExpr(t);
        }
        static String generateEnum(String ns, EnumDef e){
            StringBuilder sb=new StringBuilder();
            sb.append("using System;\n");
            sb.append("using System.Runtime.CompilerServices;\n");
            sb.append("namespace ").append(ns).append(" {\n");
            sb.append("public enum ").append(e.name).append(" { ");
            for(int i=0;i<e.items.size();i++){ if(i>0) sb.append(", "); sb.append(e.items.get(i)); }
            sb.append(" }\n");
            sb.append("public static class ").append(e.name).append("Codec {\n");
            sb.append("// Batched primitive array helpers.\n");
            sb.append("private static readonly ").append(e.name).append("[] VALUES = (").append(e.name).append("[])Enum.GetValues(typeof(").append(e.name).append("));\n");
            sb.append("[MethodImpl(MethodImplOptions.AggressiveInlining)] public static ").append(e.name).append(" FromOrdinal(int ordinal) => VALUES[ordinal];\n");
            sb.append("}\n");
            sb.append("}\n");
            return sb.toString();
        }
        static String generateStruct(String ns, Struct s){
            return generateStruct(ns, s, ns);
        }
        static String generateStruct(String ns, Struct s, String runtimeNs){
            if(Codegen.isFixedStruct(s)){
                return generateFixedStruct(ns, s, runtimeNs);
            }
            List<Field> presenceFields=Codegen.presenceFields(s.fields);
            StringBuilder sb=new StringBuilder();
            sb.append("using System; using System.IO; using System.Collections.Generic; using System.Runtime.CompilerServices; using System.Runtime.InteropServices; \n");
            if(runtimeNs!=null && !runtimeNs.isBlank() && !runtimeNs.equals(ns)){
                sb.append("using ").append(runtimeNs).append(";\n");
            }
            sb.append("namespace ").append(ns).append(" { public sealed class ").append(s.name).append(" {\n");
            for(Field f: s.fields){
                String type=csMapType(f.type);
                sb.append("  public ").append(type).append(" ").append(Codegen.cap(f.name));
                if(!isCsValueType(f.type) && !Codegen.isOptionalType(f.type)) sb.append(" = default!");
                sb.append(";\n");
            }
            sb.append("  // Fast byte[]/ReadOnlyMemory entry.\n");
            sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining)] public static ").append(s.name).append(" ReadFrom(byte[] payload){ using var r=BufUtil.FastBufferReader.Rent(payload); return ReadFrom(r); }\n");
            sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining)] public static ").append(s.name).append(" ReadFrom(ReadOnlyMemory<byte> payload){ using var r=BufUtil.FastBufferReader.Rent(payload); return ReadFrom(r); }\n");
            sb.append("  // ReadInto reuses existing object graphs on the pooled reader path.\n");
            sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining)] public static void ReadInto(byte[] payload, ").append(s.name).append(" o){ using var r=BufUtil.FastBufferReader.Rent(payload); ReadInto(r, o); }\n");
            sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining)] public static void ReadInto(ReadOnlyMemory<byte> payload, ").append(s.name).append(" o){ using var r=BufUtil.FastBufferReader.Rent(payload); ReadInto(r, o); }\n");
            appendCsReadMethodBodyWithReuse(sb, s, presenceFields, "BufUtil.FastBufferReader", "r");
            appendCsReadMethodBodyWithReuse(sb, s, presenceFields, "BinaryReader", "r");
            appendCsWriteMethodBody(sb, s, presenceFields, "BufUtil.FastBufferWriter", "w");
            sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] public BufUtil.FastBufferWriter SerializePooled(){ var w=BufUtil.FastBufferWriter.Rent(); WriteTo(w); return w; }\n");
            sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining)] public byte[] ToByteArray(){ using var w=SerializePooled(); return w.ToArray(); }\n");
            appendCsWriteMethodBody(sb, s, presenceFields, "BinaryWriter", "w");
            sb.append("} }\n");
            return sb.toString();
        }
        static String generateFixedStruct(String ns, Struct s, String runtimeNs){
            StringBuilder sb=new StringBuilder();
            sb.append("using System; using System.IO; using System.Collections.Generic; using System.Runtime.CompilerServices; using System.Runtime.InteropServices; \n");
            if(runtimeNs!=null && !runtimeNs.isBlank() && !runtimeNs.equals(ns)){
                sb.append("using ").append(runtimeNs).append(";\n");
            }
            sb.append("namespace ").append(ns).append(" { public sealed class ").append(s.name).append(" {\n");
            sb.append("    // @fixed: no presence bits; read/write fixed layout directly.\n");
            for(Field f: s.fields){
                String type=csMapType(f.type);
                sb.append("  public ").append(type).append(" ").append(Codegen.cap(f.name));
                if(!isCsValueType(f.type) && !Codegen.isOptionalType(f.type)) sb.append(" = default!");
                sb.append(";\n");
            }
            sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining)] public static ").append(s.name).append(" ReadFrom(byte[] payload){ using var r=BufUtil.FastBufferReader.Rent(payload); return ReadFrom(r); }\n");
            sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining)] public static ").append(s.name).append(" ReadFrom(ReadOnlyMemory<byte> payload){ using var r=BufUtil.FastBufferReader.Rent(payload); return ReadFrom(r); }\n");
            sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining)] public static void ReadInto(byte[] payload, ").append(s.name).append(" o){ using var r=BufUtil.FastBufferReader.Rent(payload); ReadInto(r, o); }\n");
            sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining)] public static void ReadInto(ReadOnlyMemory<byte> payload, ").append(s.name).append(" o){ using var r=BufUtil.FastBufferReader.Rent(payload); ReadInto(r, o); }\n");
            sb.append("  public static ").append(s.name).append(" ReadFrom(BufUtil.FastBufferReader r){ var o=new ").append(s.name).append("(); ReadInto(r, o); return o; }\n");
            sb.append("  public static void ReadInto(BufUtil.FastBufferReader r, ").append(s.name).append(" o){ if(o==null) throw new ArgumentNullException(nameof(o));\n");
            appendCsFixedReadStructInto(sb, s, "o", "r", "    ");
            sb.append("  }\n");
            sb.append("  public static ").append(s.name).append(" ReadFrom(BinaryReader r){ var o=new ").append(s.name).append("(); ReadInto(r, o); return o; }\n");
            sb.append("  public static void ReadInto(BinaryReader r, ").append(s.name).append(" o){ if(o==null) throw new ArgumentNullException(nameof(o));\n");
            appendCsFixedReadStructInto(sb, s, "o", "r", "    ");
            sb.append("  }\n");
            sb.append("  public void WriteTo(BufUtil.FastBufferWriter w){\n");
            appendCsFixedWriteStructValue(sb, s, "this", "w", "    ");
            sb.append("  }\n");
            sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] public BufUtil.FastBufferWriter SerializePooled(){ var w=BufUtil.FastBufferWriter.Rent(); WriteTo(w); return w; }\n");
            sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining)] public byte[] ToByteArray(){ using var w=SerializePooled(); return w.ToArray(); }\n");
            sb.append("  public void WriteTo(BinaryWriter w){\n");
            appendCsFixedWriteStructValue(sb, s, "this", "w", "    ");
            sb.append("  }\n");
            sb.append("} }\n");
            return sb.toString();
        }
        static void appendCsFixedReadStructInto(StringBuilder sb, Struct s, String targetExpr, String readerVar, String indent){
            for(Field field: s.fields){
                appendCsFixedReadField(sb, targetExpr+"."+Codegen.cap(field.name), field.type, readerVar, indent);
            }
        }
        static void appendCsFixedWriteStructValue(StringBuilder sb, Struct s, String valueExpr, String writerVar, String indent){
            for(Field field: s.fields){
                appendCsFixedWriteField(sb, valueExpr+"."+Codegen.cap(field.name), field.type, writerVar, indent);
            }
        }
        static void appendCsFixedReadField(StringBuilder sb, String targetExpr, String t, String readerVar, String indent){
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2).trim();
                if(Codegen.isStructType(inner) && Codegen.isFixedStruct(Codegen.structDef(inner))){
                    String countVar=Codegen.childVar(targetExpr, "count");
                    String tmpVar=Codegen.childVar(targetExpr, "tmp");
                    String indexVar=Codegen.childVar(targetExpr, "index");
                    String elemVar=Codegen.childVar(targetExpr, "elem");
                    sb.append(indent).append("{\n");
                    sb.append(indent).append("  int ").append(countVar).append("=BufUtil.ReadSize(").append(readerVar).append(");\n");
                    sb.append(indent).append("  ").append(csMapType(t)).append(" ").append(tmpVar).append("=new ").append(csMapType(inner)).append("[").append(countVar).append("];\n");
                    sb.append(indent).append("  for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                    sb.append(indent).append("    ").append(inner).append(" ").append(elemVar).append("=new ").append(inner).append("();\n");
                    if(Codegen.isInlineStructType(inner)){
                        appendCsFixedReadStructInto(sb, Codegen.structDef(inner), elemVar, readerVar, indent+"    ");
                    }else{
                        sb.append(indent).append("    ").append(inner).append(".ReadInto(").append(readerVar).append(", ").append(elemVar).append(");\n");
                    }
                    sb.append(indent).append("    ").append(tmpVar).append("[").append(indexVar).append("]=").append(elemVar).append(";\n");
                    sb.append(indent).append("  }\n");
                    sb.append(indent).append("  ").append(targetExpr).append("=").append(tmpVar).append(";\n");
                    sb.append(indent).append("}\n");
                    return;
                }
                sb.append(indent).append(targetExpr).append("=").append(csReadFixedExpr(t, readerVar)).append(";\n");
                return;
            }
            if(Codegen.isStructType(t)){
                String reuseVar=Codegen.childVar(targetExpr, "reuse");
                sb.append(indent).append(t).append(" ").append(reuseVar).append("=").append(targetExpr).append(" ?? new ").append(t).append("();\n");
                sb.append(indent).append(targetExpr).append("=").append(reuseVar).append(";\n");
                if(Codegen.isInlineStructType(t) && Codegen.isFixedStruct(Codegen.structDef(t))){
                    appendCsFixedReadStructInto(sb, Codegen.structDef(t), reuseVar, readerVar, indent);
                }else{
                    sb.append(indent).append(t).append(".ReadInto(").append(readerVar).append(", ").append(reuseVar).append(");\n");
                }
                return;
            }
            sb.append(indent).append(targetExpr).append("=").append(csReadFixedExpr(t, readerVar)).append(";\n");
        }
        static void appendCsFixedWriteField(StringBuilder sb, String valueExpr, String t, String writerVar, String indent){
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2).trim();
                if(Codegen.isStructType(inner) && Codegen.isFixedStruct(Codegen.structDef(inner))){
                    String valuesVar=Codegen.childVar(valueExpr, "values");
                    String countVar=Codegen.childVar(valueExpr, "count");
                    String indexVar=Codegen.childVar(valueExpr, "index");
                    String elemVar=Codegen.childVar(valueExpr, "elem");
                    sb.append(indent).append("{\n");
                    sb.append(indent).append("  ").append(csMapType(t)).append(" ").append(valuesVar).append("=").append(valueExpr).append(" ?? Array.Empty<").append(csMapType(inner)).append(">();\n");
                    sb.append(indent).append("  int ").append(countVar).append("=").append(valuesVar).append(".Length;\n");
                    sb.append(indent).append("  BufUtil.WriteSize(").append(writerVar).append(", ").append(countVar).append(");\n");
                    sb.append(indent).append("  for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                    sb.append(indent).append("    ").append(inner).append(" ").append(elemVar).append("=").append(valuesVar).append("[").append(indexVar).append("] ?? new ").append(inner).append("();\n");
                    if(Codegen.isInlineStructType(inner)){
                        appendCsFixedWriteStructValue(sb, Codegen.structDef(inner), elemVar, writerVar, indent+"    ");
                    }else{
                        sb.append(indent).append("    ").append(elemVar).append(".WriteTo(").append(writerVar).append(");\n");
                    }
                    sb.append(indent).append("  }\n");
                    sb.append(indent).append("}\n");
                    return;
                }
                sb.append(indent).append(csWriteFixedStmt(valueExpr, t, writerVar)).append(";\n");
                return;
            }
            if(Codegen.isStructType(t)){
                String valueVar=Codegen.childVar(valueExpr, "value");
                sb.append(indent).append(t).append(" ").append(valueVar).append("=").append(valueExpr).append(" ?? new ").append(t).append("();\n");
                if(Codegen.isInlineStructType(t) && Codegen.isFixedStruct(Codegen.structDef(t))){
                    appendCsFixedWriteStructValue(sb, Codegen.structDef(t), valueVar, writerVar, indent);
                }else{
                    sb.append(indent).append(valueVar).append(".WriteTo(").append(writerVar).append(");\n");
                }
                return;
            }
            sb.append(indent).append(csWriteFixedStmt(valueExpr, t, writerVar)).append(";\n");
        }
        static void appendCsInlineReadStructValue(StringBuilder sb, String targetExpr, Struct nested, String readerVar, String indent, boolean hot){
            if(nested==null){
                throw new IllegalStateException("missing inline struct metadata for "+targetExpr);
            }
            if(Codegen.isFixedStruct(nested)){
                String reuseVar=Codegen.childVar(targetExpr, "reuse");
                sb.append(indent).append("{\n");
                sb.append(indent).append("  ").append(nested.name).append(" ").append(reuseVar).append("=").append(targetExpr).append(" ?? new ").append(nested.name).append("();\n");
                sb.append(indent).append("  ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                appendCsFixedReadStructInto(sb, nested, reuseVar, readerVar, indent+"  ");
                sb.append(indent).append("}\n");
                return;
            }
            List<Field> nestedPresence=Codegen.presenceFields(nested.fields);
            String reuseVar=Codegen.childVar(targetExpr, "reuse");
            String presenceVar=Codegen.childVar(targetExpr, "presence");
            sb.append(indent).append("{\n");
            sb.append(indent).append("  ").append(nested.name).append(" ").append(reuseVar).append("=").append(targetExpr).append(" ?? new ").append(nested.name).append("();\n");
            sb.append(indent).append("  ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            appendCsPresenceReadPrelude(sb, nestedPresence.size(), readerVar, indent+"  ", presenceVar);
            int presenceIndex=0;
            for(Field field: nested.fields){
                String fieldExpr=reuseVar+"."+Codegen.cap(field.name);
                if(Codegen.isPresenceTrackedType(field.type)){
                    String presentExpr=csPresenceExpr(presenceVar, presenceIndex++, nestedPresence.size());
                    if(Codegen.isOptionalType(field.type)){
                        String inner=Codegen.genericBody(field.type).trim();
                        sb.append(indent).append("  if(").append(presentExpr).append("){\n");
                        appendCsAssignReadExistingValue(sb, fieldExpr, inner, readerVar, indent+"    ", hot);
                        sb.append(indent).append("  }else{\n");
                        sb.append(indent).append("    ").append(fieldExpr).append("=default;\n");
                        sb.append(indent).append("  }\n");
                    }else{
                        sb.append(indent).append("  if(").append(presentExpr).append("){\n");
                        appendCsAssignReadExistingValue(sb, fieldExpr, field.type, readerVar, indent+"    ", hot);
                        sb.append(indent).append("  }else{\n");
                        appendCsResetReadValue(sb, fieldExpr, field.type, indent+"    ");
                        sb.append(indent).append("  }\n");
                    }
                }else{
                    appendCsAssignReadExistingValue(sb, fieldExpr, field.type, readerVar, indent+"  ", hot);
                }
            }
            sb.append(indent).append("}\n");
        }
        static void appendCsInlineWriteStructValue(StringBuilder sb, String valueExpr, Struct nested, String writerVar, String indent, boolean hot){
            if(nested==null){
                throw new IllegalStateException("missing inline struct metadata for "+valueExpr);
            }
            String valueVar=Codegen.childVar(valueExpr, "value");
            String presenceVar=Codegen.childVar(valueExpr, "presence");
            sb.append(indent).append("{\n");
            sb.append(indent).append("  ").append(nested.name).append(" ").append(valueVar).append("=").append(valueExpr).append(" ?? new ").append(nested.name).append("();\n");
            if(Codegen.isFixedStruct(nested)){
                appendCsFixedWriteStructValue(sb, nested, valueVar, writerVar, indent+"  ");
            }else{
                List<Field> nestedPresence=Codegen.presenceFields(nested.fields);
                appendCsPresenceWritePrelude(sb, nestedPresence, valueVar+".", writerVar, indent+"  ", presenceVar);
                for(Field field: nested.fields){
                    String fieldExpr=valueVar+"."+Codegen.cap(field.name);
                    if(Codegen.isOptionalType(field.type)){
                        sb.append(indent).append("  if(").append(fieldExpr).append(" != null){\n");
                        appendCsWriteStatements(sb, csOptionalValueExpr(fieldExpr, Codegen.genericBody(field.type).trim()), Codegen.genericBody(field.type).trim(), writerVar, indent+"    ", hot);
                        sb.append(indent).append("  }\n");
                    }else if(Codegen.isPresenceTrackedType(field.type)){
                        sb.append(indent).append("  if(").append(csHasWireValueExpr(fieldExpr, field.type)).append("){\n");
                        appendCsWriteStatements(sb, fieldExpr, field.type, writerVar, indent+"    ", hot);
                        sb.append(indent).append("  }\n");
                    }else{
                        appendCsWriteStatements(sb, fieldExpr, field.type, writerVar, indent+"  ", hot);
                    }
                }
            }
            sb.append(indent).append("}\n");
        }
        static void appendCsReadMethodBody(StringBuilder sb, Struct s, List<Field> presenceFields, String readerType, String readerVar){
            sb.append("    // Read order must match writeTo exactly.\n");
            sb.append("  public static ").append(s.name).append(" ReadFrom(").append(readerType).append(" ").append(readerVar).append("){ var o=new ").append(s.name).append("();\n");
            appendCsPresenceReadPrelude(sb, presenceFields.size(), readerVar, "    ");
            int presenceIndex=0;
            for(Field f: s.fields){
                if(Codegen.isPresenceTrackedType(f.type)){
                    String presentExpr=csPresenceExpr("__presence", presenceIndex++, presenceFields.size());
                    if(Codegen.isOptionalType(f.type)){
                        String inner=Codegen.genericBody(f.type).trim();
                        sb.append("    if(").append(presentExpr).append("){\n");
                        appendCsReadValueToLocal(sb, "__value", inner, readerVar, "      ", s.hot);
                        sb.append("      o.").append(Codegen.cap(f.name)).append("=__value;\n");
                        sb.append("    }else{\n");
                        sb.append("      o.").append(Codegen.cap(f.name)).append("=default;\n");
                        sb.append("    }\n");
                    }else{
                        sb.append("    if(").append(presentExpr).append("){\n");
                        appendCsAssignReadValue(sb, "o."+Codegen.cap(f.name), f, readerVar, "      ", s.hot);
                        sb.append("    }else{\n");
                        sb.append("      o.").append(Codegen.cap(f.name)).append("=").append(csDefaultValueExpr(f.type)).append(";\n");
                        sb.append("    }\n");
                    }
                }else{
                    appendCsAssignReadValue(sb, "o."+Codegen.cap(f.name), f, readerVar, "    ", s.hot);
                }
            }
            sb.append("    return o; }\n");
        }
        static void appendCsReadMethodBodyWithReuse(StringBuilder sb, Struct s, List<Field> presenceFields, String readerType, String readerVar){
            sb.append("    // Read order must match writeTo exactly.\n");
            if("BufUtil.FastBufferReader".equals(readerType)){
                sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] ");
            }else{
                sb.append("  ");
            }
            sb.append("public static ").append(s.name).append(" ReadFrom(").append(readerType).append(" ").append(readerVar).append("){ var o=new ").append(s.name).append("(); ReadInto(").append(readerVar).append(", o); return o; }\n");
            sb.append("  // ReadInto reuses existing object graphs and resets missing fields.\n");
            if("BufUtil.FastBufferReader".equals(readerType)){
                sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] ");
            }else{
                sb.append("  ");
            }
            sb.append("public static void ReadInto(").append(readerType).append(" ").append(readerVar).append(", ").append(s.name).append(" o){ if(o==null) throw new ArgumentNullException(nameof(o));\n");
            appendCsPresenceReadPrelude(sb, presenceFields.size(), readerVar, "    ");
            int presenceIndex=0;
            for(Field f: s.fields){
                String fieldExpr="o."+Codegen.cap(f.name);
                if(Codegen.isPresenceTrackedType(f.type)){
                    String presentExpr=csPresenceExpr("__presence", presenceIndex++, presenceFields.size());
                    if(Codegen.isOptionalType(f.type)){
                        String inner=Codegen.genericBody(f.type).trim();
                        sb.append("    if(").append(presentExpr).append("){\n");
                        appendCsAssignReadExistingValue(sb, fieldExpr, inner, readerVar, "      ", s.hot);
                        sb.append("    }else{\n");
                        sb.append("      ").append(fieldExpr).append("=default;\n");
                        sb.append("    }\n");
                    }else{
                        sb.append("    if(").append(presentExpr).append("){\n");
                        appendCsAssignReadExistingValue(sb, fieldExpr, f, readerVar, "      ", s.hot);
                        sb.append("    }else{\n");
                        appendCsResetReadValue(sb, fieldExpr, f.type, "      ");
                        sb.append("    }\n");
                    }
                }else{
                    appendCsAssignReadExistingValue(sb, fieldExpr, f, readerVar, "    ", s.hot);
                }
            }
            sb.append("  }\n");
        }
        static void appendCsWriteMethodBody(StringBuilder sb, Struct s, List<Field> presenceFields, String writerType, String writerVar){
            sb.append("    // Write order must match wire layout exactly.\n");
            if("BufUtil.FastBufferWriter".equals(writerType)){
                sb.append("  [MethodImpl(MethodImplOptions.AggressiveInlining | MethodImplOptions.AggressiveOptimization)] ");
            }else{
                sb.append("  ");
            }
            sb.append("public void WriteTo(").append(writerType).append(" ").append(writerVar).append("){\n");
            appendCsPresenceWritePrelude(sb, presenceFields, "this.", writerVar, "    ");
            for(Field f: s.fields){
                String fieldExpr="this."+Codegen.cap(f.name);
                if(Codegen.isOptionalType(f.type)){
                    String cachedVar=csPresenceValueVar(f);
                    sb.append("    if(").append(csPresenceHasVar(f)).append("){\n");
                    appendCsWriteStatements(sb, csStableWriteValueExpr(cachedVar, f.type), Codegen.genericBody(f.type).trim(), writerVar, "      ", s.hot);
                    sb.append("    }\n");
                }else if(Codegen.isPresenceTrackedType(f.type)){
                    String cachedVar=csPresenceValueVar(f);
                    sb.append("    if(").append(csPresenceHasVar(f)).append("){\n");
                    appendCsWriteStatements(sb, csStableWriteValueExpr(cachedVar, f.type), f, writerVar, "      ", s.hot);
                    sb.append("    }\n");
                }else{
                    appendCsWriteStatements(sb, fieldExpr, f, writerVar, "    ", s.hot);
                }
            }
            sb.append("  }\n");
        }
        static void appendCsReadValueToLocal(StringBuilder sb, String localName, String t, String readerVar, String indent, boolean hot){
            sb.append(indent).append(csMapType(t)).append(" ").append(localName).append(";\n");
            appendCsAssignReadValue(sb, localName, t, readerVar, indent, hot);
        }
        static void appendCsAssignReadValue(StringBuilder sb, String targetExpr, Field f, String readerVar, String indent, boolean hot){
            if(Codegen.isPackedPrimitiveListField(f) || Codegen.isPackedPrimitiveMapField(f)){
                sb.append(indent).append(targetExpr).append("=").append(csReadExpr(f, readerVar)).append(";\n");
                return;
            }
            appendCsAssignReadValue(sb, targetExpr, f.type, readerVar, indent, hot);
        }
        static void appendCsAssignReadValue(StringBuilder sb, String targetExpr, String t, String readerVar, String indent, boolean hot){
            if(Codegen.isStructType(t) && Codegen.isInlineStructType(t)){
                appendCsInlineReadStructValue(sb, targetExpr, Codegen.structDef(t), readerVar, indent, hot);
                return;
            }
            if(Codegen.isHotExpandedType(t)){
                appendCsHotReadValue(sb, targetExpr, t, readerVar, indent, true);
                return;
            }
            sb.append(indent).append(targetExpr).append("=").append(csReadExpr(t, readerVar)).append(";\n");
        }
        static void appendCsAssignReadExistingValue(StringBuilder sb, String targetExpr, Field f, String readerVar, String indent, boolean hot){
            if(Codegen.isPackedPrimitiveListField(f) || Codegen.isPackedPrimitiveMapField(f)){
                sb.append(indent).append(targetExpr).append("=").append(csReadExpr(f, readerVar, targetExpr)).append(";\n");
                return;
            }
            appendCsAssignReadExistingValue(sb, targetExpr, f.type, readerVar, indent, hot);
        }
        static void appendCsAssignReadExistingValue(StringBuilder sb, String targetExpr, String t, String readerVar, String indent, boolean hot){
            if(Codegen.isStructType(t) && Codegen.isInlineStructType(t)){
                appendCsInlineReadStructValue(sb, targetExpr, Codegen.structDef(t), readerVar, indent, hot);
                return;
            }
            if(Codegen.isOptionalType(t)){
                sb.append(indent).append(targetExpr).append("=").append(csReadExpr(t, readerVar)).append(";\n");
                return;
            }
            if(isCsReusableObjectType(t)){
                String reuseVar=Codegen.childVar(targetExpr, "reuse");
                sb.append(indent).append(csMapType(t)).append("? ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                sb.append(indent).append("if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("  ").append(reuseVar).append("=new ").append(csMapType(t)).append("();\n");
                sb.append(indent).append("  ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("}\n");
                sb.append(indent).append(t).append(".ReadInto(").append(readerVar).append(", ").append(reuseVar).append("!);\n");
                return;
            }
            if(t.endsWith("[]")){
                appendCsReadExistingArrayValue(sb, targetExpr, t, readerVar, indent, hot);
                return;
            }
            if(Codegen.isListLikeType(t)){
                appendCsReadExistingListValue(sb, targetExpr, t, readerVar, indent, hot);
                return;
            }
            if(Codegen.isQueueLikeType(t)){
                appendCsReadExistingQueueValue(sb, targetExpr, t, readerVar, indent, hot);
                return;
            }
            if(Codegen.isSetLikeType(t)){
                appendCsReadExistingSetValue(sb, targetExpr, t, readerVar, indent, hot);
                return;
            }
            if(Codegen.isMapLikeType(t)){
                appendCsReadExistingMapValue(sb, targetExpr, t, readerVar, indent, hot);
                return;
            }
            sb.append(indent).append(targetExpr).append("=").append(csReadExpr(t, readerVar)).append(";\n");
        }
        static void appendCsResetReadValue(StringBuilder sb, String targetExpr, String t, String indent){
            if(t.endsWith("[]")){
                sb.append(indent).append(targetExpr).append("=").append(csDefaultValueExpr(t)).append(";\n");
                return;
            }
            if(Codegen.isListLikeType(t) || Codegen.isQueueLikeType(t) || Codegen.isSetLikeType(t) || Codegen.isMapLikeType(t)){
                String reuseVar=Codegen.childVar(targetExpr, "reuse");
                sb.append(indent).append("{\n");
                sb.append(indent).append("  ").append(csMapType(t)).append("? ").append(reuseVar).append("=").append(targetExpr).append(";\n");
                sb.append(indent).append("  if(").append(reuseVar).append("!=null){\n");
                if(Codegen.isListLikeType(t) && !"LinkedList".equals(Codegen.canonicalContainerType(t))){
                    sb.append(indent).append("    BufUtil.RecycleList(").append(reuseVar).append(");\n");
                    sb.append(indent).append("    ").append(targetExpr).append("=").append(csBorrowCollectionExpr(t, "0")).append(";\n");
                }else if(Codegen.isQueueLikeType(t)){
                    sb.append(indent).append("    BufUtil.RecycleList(").append(reuseVar).append(");\n");
                    sb.append(indent).append("    ").append(targetExpr).append("=").append(csBorrowCollectionExpr(t, "0")).append(";\n");
                }else if(Codegen.isSetLikeType(t)){
                    sb.append(indent).append("    BufUtil.RecycleHashSet(").append(reuseVar).append(");\n");
                    sb.append(indent).append("    ").append(targetExpr).append("=").append(csBorrowCollectionExpr(t, "0")).append(";\n");
                }else if(Codegen.isMapLikeType(t)){
                    sb.append(indent).append("    BufUtil.RecycleDictionary(").append(reuseVar).append(");\n");
                    sb.append(indent).append("    ").append(targetExpr).append("=").append(csBorrowCollectionExpr(t, "0")).append(";\n");
                }else{
                    sb.append(indent).append("    ").append(reuseVar).append(".Clear();\n");
                }
                sb.append(indent).append("  }else{\n");
                sb.append(indent).append("    ").append(targetExpr).append("=").append(csDefaultValueExpr(t)).append(";\n");
                sb.append(indent).append("  }\n");
                sb.append(indent).append("}\n");
                return;
            }
            sb.append(indent).append(targetExpr).append("=").append(csDefaultValueExpr(t)).append(";\n");
        }
        static void appendCsReadExistingArrayValue(StringBuilder sb, String targetExpr, String t, String readerVar, String indent, boolean hot){
            String inner=t.substring(0, t.length()-2).trim();
            if(inner.equals("int")||inner.equals("Integer")||inner.equals("long")||inner.equals("Long")
                    || inner.equals("byte")||inner.equals("Byte")||inner.equals("short")||inner.equals("Short")
                    || inner.equals("boolean")||inner.equals("Boolean")||inner.equals("char")||inner.equals("Character")
                    || inner.equals("float")||inner.equals("Float")||inner.equals("double")||inner.equals("Double")){
                sb.append(indent).append(targetExpr).append("=").append(csReadIntoArrayExpr(t, readerVar, targetExpr)).append(";\n");
                return;
            }
            String countVar=Codegen.childVar(targetExpr, "count");
            String reuseVar=Codegen.childVar(targetExpr, "reuse");
            String indexVar=Codegen.childVar(targetExpr, "index");
            sb.append(indent).append("{\n");
            sb.append(indent).append("  int ").append(countVar).append("=BufUtil.ReadSize(").append(readerVar).append(");\n");
            sb.append(indent).append("  ").append(csMapType(t)).append("? ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("  if(").append(countVar).append("==0){\n");
            sb.append(indent).append("    ").append(targetExpr).append("=System.Array.Empty<").append(csMapType(inner)).append(">();\n");
            sb.append(indent).append("  }else{\n");
            sb.append(indent).append("    if(").append(reuseVar).append("==null || ").append(reuseVar).append(".Length!=").append(countVar).append("){\n");
            sb.append(indent).append("      ").append(reuseVar).append("=new ").append(csMapType(inner)).append("[").append(countVar).append("];\n");
            sb.append(indent).append("      ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            appendCsAssignReadExistingValue(sb, reuseVar+"["+indexVar+"]", inner, readerVar, indent+"      ", hot);
            sb.append(indent).append("    }\n");
            sb.append(indent).append("  }\n");
            sb.append(indent).append("}\n");
        }
        static void appendCsReadExistingListValue(StringBuilder sb, String targetExpr, String t, String readerVar, String indent, boolean hot){
            String inner=Codegen.genericBody(t).trim();
            String canonical=Codegen.canonicalContainerType(t);
            String listType=csMapType(t);
            String countVar=Codegen.childVar(targetExpr, "count");
            String reuseVar=Codegen.childVar(targetExpr, "reuse");
            String existingCountVar=Codegen.childVar(targetExpr, "existingCount");
            String existingSpanVar=Codegen.childVar(targetExpr, "existingSpan");
            String indexVar=Codegen.childVar(targetExpr, "index");
            String elemVar=Codegen.childVar(targetExpr, "elem");
            sb.append(indent).append("{\n");
            sb.append(indent).append("  int ").append(countVar).append("=BufUtil.ReadSize(").append(readerVar).append(");\n");
            sb.append(indent).append("  ").append(listType).append("? ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            if("LinkedList".equals(canonical)){
                sb.append(indent).append("  if(").append(reuseVar).append("==null){\n");
                sb.append(indent).append("    ").append(reuseVar).append("=new LinkedList<").append(csMapType(inner)).append(">();\n");
                sb.append(indent).append("    ").append(targetExpr).append("=").append(reuseVar).append(";\n");
                sb.append(indent).append("  }else{\n");
                sb.append(indent).append("    ").append(reuseVar).append(".Clear();\n");
                sb.append(indent).append("  }\n");
                sb.append(indent).append("  for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendCsReadValueToLocal(sb, elemVar, inner, readerVar, indent+"    ", hot);
                sb.append(indent).append("    ").append(reuseVar).append(".AddLast(").append(elemVar).append(");\n");
                sb.append(indent).append("  }\n");
                sb.append(indent).append("}\n");
                return;
            }
            sb.append(indent).append("  if(").append(reuseVar).append("==null){\n");
            sb.append(indent).append("    ").append(reuseVar).append("=").append(csBorrowCollectionExpr(t, countVar)).append(";\n");
            sb.append(indent).append("    ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("  }\n");
            sb.append(indent).append("  if(").append(reuseVar).append(".Capacity<").append(countVar).append(") ").append(reuseVar).append(".Capacity=").append(countVar).append(";\n");
            sb.append(indent).append("  // Do not Clear here; overwrite prefix then trim tail.\n");
            sb.append(indent).append("  int ").append(existingCountVar).append("=").append(reuseVar).append(".Count;\n");
            sb.append(indent).append("  var ").append(existingSpanVar).append("=CollectionsMarshal.AsSpan(").append(reuseVar).append(");\n");
            sb.append(indent).append("  for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            sb.append(indent).append("    if(").append(indexVar).append("<").append(existingCountVar).append("){\n");
            appendCsAssignReadExistingValue(sb, existingSpanVar+"["+indexVar+"]", inner, readerVar, indent+"      ", hot);
            sb.append(indent).append("    }else{\n");
            appendCsReadValueToLocal(sb, elemVar, inner, readerVar, indent+"      ", hot);
            sb.append(indent).append("      ").append(reuseVar).append(".Add(").append(elemVar).append(");\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("  }\n");
            sb.append(indent).append("  if(").append(existingCountVar).append(">").append(countVar).append("){\n");
            sb.append(indent).append("    ").append(reuseVar).append(".RemoveRange(").append(countVar).append(", ").append(existingCountVar).append("-").append(countVar).append(");\n");
            sb.append(indent).append("  }\n");
            sb.append(indent).append("}\n");
        }
        static void appendCsReadExistingQueueValue(StringBuilder sb, String targetExpr, String t, String readerVar, String indent, boolean hot){
            String inner=Codegen.genericBody(t).trim();
            String listType=csMapType(t);
            String countVar=Codegen.childVar(targetExpr, "count");
            String reuseVar=Codegen.childVar(targetExpr, "reuse");
            String existingCountVar=Codegen.childVar(targetExpr, "existingCount");
            String existingSpanVar=Codegen.childVar(targetExpr, "existingSpan");
            String indexVar=Codegen.childVar(targetExpr, "index");
            String elemVar=Codegen.childVar(targetExpr, "elem");
            sb.append(indent).append("{\n");
            sb.append(indent).append("  int ").append(countVar).append("=BufUtil.ReadSize(").append(readerVar).append(");\n");
            sb.append(indent).append("  ").append(listType).append("? ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("  if(").append(reuseVar).append("==null){\n");
            sb.append(indent).append("    ").append(reuseVar).append("=").append(csBorrowCollectionExpr(t, countVar)).append(";\n");
            sb.append(indent).append("    ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("  }\n");
            sb.append(indent).append("  if(").append(reuseVar).append(".Capacity<").append(countVar).append(") ").append(reuseVar).append(".Capacity=").append(countVar).append(";\n");
            sb.append(indent).append("  int ").append(existingCountVar).append("=").append(reuseVar).append(".Count;\n");
            sb.append(indent).append("  var ").append(existingSpanVar).append("=CollectionsMarshal.AsSpan(").append(reuseVar).append(");\n");
            sb.append(indent).append("  for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            sb.append(indent).append("    if(").append(indexVar).append("<").append(existingCountVar).append("){\n");
            appendCsAssignReadExistingValue(sb, existingSpanVar+"["+indexVar+"]", inner, readerVar, indent+"      ", hot);
            sb.append(indent).append("    }else{\n");
            appendCsReadValueToLocal(sb, elemVar, inner, readerVar, indent+"      ", hot);
            sb.append(indent).append("      ").append(reuseVar).append(".Add(").append(elemVar).append(");\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("  }\n");
            sb.append(indent).append("  if(").append(existingCountVar).append(">").append(countVar).append("){\n");
            sb.append(indent).append("    ").append(reuseVar).append(".RemoveRange(").append(countVar).append(", ").append(existingCountVar).append("-").append(countVar).append(");\n");
            sb.append(indent).append("  }\n");
            sb.append(indent).append("}\n");
        }
        static void appendCsReadExistingSetValue(StringBuilder sb, String targetExpr, String t, String readerVar, String indent, boolean hot){
            String inner=Codegen.genericBody(t).trim();
            String setType=csMapType(t);
            String countVar=Codegen.childVar(targetExpr, "count");
            String reuseVar=Codegen.childVar(targetExpr, "reuse");
            String indexVar=Codegen.childVar(targetExpr, "index");
            String elemVar=Codegen.childVar(targetExpr, "elem");
            sb.append(indent).append("{\n");
            sb.append(indent).append("  int ").append(countVar).append("=BufUtil.ReadSize(").append(readerVar).append(");\n");
            sb.append(indent).append("  ").append(setType).append("? ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("  if(").append(reuseVar).append("==null){\n");
            sb.append(indent).append("    ").append(reuseVar).append("=").append(csBorrowCollectionExpr(t, countVar)).append(";\n");
            sb.append(indent).append("    ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("  }else{\n");
            sb.append(indent).append("    ").append(reuseVar).append(".Clear();\n");
            sb.append(indent).append("    ").append(reuseVar).append(".EnsureCapacity(").append(countVar).append(");\n");
            sb.append(indent).append("  }\n");
            sb.append(indent).append("  for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            appendCsReadValueToLocal(sb, elemVar, inner, readerVar, indent+"    ", hot);
            sb.append(indent).append("    ").append(reuseVar).append(".Add(").append(elemVar).append(");\n");
            sb.append(indent).append("  }\n");
            sb.append(indent).append("}\n");
        }
        static void appendCsReadExistingMapValue(StringBuilder sb, String targetExpr, String t, String readerVar, String indent, boolean hot){
            List<String> kv=Codegen.splitTopLevel(Codegen.genericBody(t), ',');
            String keyType=kv.get(0).trim();
            String valueType=kv.get(1).trim();
            if(hot && isCsReusableObjectType(valueType)){
                appendCsReadExistingReusableMapValue(sb, targetExpr, t, readerVar, indent, keyType, valueType);
                return;
            }
            String mapType=csMapType(t);
            String countVar=Codegen.childVar(targetExpr, "count");
            String reuseVar=Codegen.childVar(targetExpr, "reuse");
            String indexVar=Codegen.childVar(targetExpr, "index");
            String keyVar=Codegen.childVar(targetExpr, "key");
            String valueVar=Codegen.childVar(targetExpr, "value");
            sb.append(indent).append("{\n");
            sb.append(indent).append("  int ").append(countVar).append("=BufUtil.ReadSize(").append(readerVar).append(");\n");
            sb.append(indent).append("  ").append(mapType).append("? ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("  if(").append(reuseVar).append("==null){\n");
            sb.append(indent).append("    ").append(reuseVar).append("=").append(csBorrowCollectionExpr(t, countVar)).append(";\n");
            sb.append(indent).append("    ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("  }else{\n");
            sb.append(indent).append("    ").append(reuseVar).append(".Clear();\n");
            sb.append(indent).append("  }\n");
            sb.append(indent).append("  for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            appendCsReadValueToLocal(sb, keyVar, keyType, readerVar, indent+"    ", hot);
            appendCsReadValueToLocal(sb, valueVar, valueType, readerVar, indent+"    ", hot);
            sb.append(indent).append("    ").append(reuseVar).append(".Add(").append(keyVar).append(", ").append(valueVar).append(");\n");
            sb.append(indent).append("  }\n");
            sb.append(indent).append("}\n");
        }
        static void appendCsReadExistingReusableMapValue(StringBuilder sb, String targetExpr, String t, String readerVar, String indent, String keyType, String valueType){
            String mapType=csMapType(t);
            String countVar=Codegen.childVar(targetExpr, "count");
            String reuseVar=Codegen.childVar(targetExpr, "reuse");
            String indexVar=Codegen.childVar(targetExpr, "index");
            String keyVar=Codegen.childVar(targetExpr, "key");
            String valueVar=Codegen.childVar(targetExpr, "value");
            String keysVar=Codegen.childVar(targetExpr, "keys");
            String valuesVar=Codegen.childVar(targetExpr, "values");
            String keyCsType=csMapType(keyType);
            String valueCsType=csMapType(valueType);
            sb.append(indent).append("{\n");
            sb.append(indent).append("  int ").append(countVar).append("=BufUtil.ReadSize(").append(readerVar).append(");\n");
            sb.append(indent).append("  ").append(mapType).append("? ").append(reuseVar).append("=").append(targetExpr).append(";\n");
            sb.append(indent).append("  if(").append(reuseVar).append("==null){\n");
            sb.append(indent).append("    ").append(reuseVar).append("=").append(csBorrowCollectionExpr(t, countVar)).append(";\n");
            sb.append(indent).append("    ").append(targetExpr).append("=").append(reuseVar).append(";\n");
            sb.append(indent).append("  }\n");
            sb.append(indent).append("  if(").append(countVar).append("==0){\n");
            sb.append(indent).append("    ").append(reuseVar).append(".Clear();\n");
            sb.append(indent).append("  }else{\n");
            sb.append(indent).append("    ").append(keyCsType).append("[] ").append(keysVar).append("=System.Buffers.ArrayPool<").append(keyCsType).append(">.Shared.Rent(Math.Max(1, ").append(countVar).append("));\n");
            sb.append(indent).append("    ").append(valueCsType).append("?[] ").append(valuesVar).append("=System.Buffers.ArrayPool<").append(valueCsType).append("?>.Shared.Rent(Math.Max(1, ").append(countVar).append("));\n");
            sb.append(indent).append("      // Small hot dictionary reuses value objects by key before rebuilding.\n");
            sb.append(indent).append("\n");
            sb.append(indent).append("    try{\n");
            sb.append(indent).append("      for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            appendCsReadValueToLocal(sb, keyVar, keyType, readerVar, indent+"        ", true);
            sb.append(indent).append("        ").append(keysVar).append("[").append(indexVar).append("]=").append(keyVar).append(";\n");
            sb.append(indent).append("        ").append(valueCsType).append("? ").append(valueVar).append("=").append(reuseVar).append(".TryGetValue(").append(keyVar).append(", out var __existingValue) ? __existingValue : null;\n");
            sb.append(indent).append("        if(").append(valueVar).append("==null){\n");
            sb.append(indent).append("          ").append(valueVar).append("=new ").append(valueCsType).append("();\n");
            sb.append(indent).append("        }\n");
            sb.append(indent).append("        ").append(valueType).append(".ReadInto(").append(readerVar).append(", ").append(valueVar).append("!);\n");
            sb.append(indent).append("        ").append(valuesVar).append("[").append(indexVar).append("]=").append(valueVar).append(";\n");
            sb.append(indent).append("        }\n");
            sb.append(indent).append("      ").append(reuseVar).append(".Clear();\n");
            sb.append(indent).append("      ").append(reuseVar).append(".EnsureCapacity(").append(countVar).append(");\n");
            sb.append(indent).append("      for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
            sb.append(indent).append("        ").append(reuseVar).append(".Add(").append(keysVar).append("[").append(indexVar).append("], ").append(valuesVar).append("[").append(indexVar).append("]!);\n");
            sb.append(indent).append("      }\n");
            sb.append(indent).append("    }finally{\n");
            sb.append(indent).append("      System.Buffers.ArrayPool<").append(keyCsType).append(">.Shared.Return(").append(keysVar).append(", clearArray:false);\n");
            sb.append(indent).append("      System.Buffers.ArrayPool<").append(valueCsType).append("?>.Shared.Return(").append(valuesVar).append(", clearArray:true);\n");
            sb.append(indent).append("    }\n");
            sb.append(indent).append("      // Large dictionary keeps the clear-and-rebuild path.\n");
            sb.append(indent).append("  }\n");
            sb.append(indent).append("}\n");
        }
        static String csReadIntoArrayExpr(String t, String readerVar, String targetExpr){
            String inner=t.substring(0, t.length()-2).trim();
            if(inner.equals("int")||inner.equals("Integer")) return "BufUtil.ReadIntArrayInto("+readerVar+", "+targetExpr+")";
            if(inner.equals("long")||inner.equals("Long")) return "BufUtil.ReadLongArrayInto("+readerVar+", "+targetExpr+")";
            if(inner.equals("byte")||inner.equals("Byte")) return "BufUtil.ReadBytesInto("+readerVar+", "+targetExpr+")";
            if(inner.equals("short")||inner.equals("Short")) return "BufUtil.ReadShortArrayInto("+readerVar+", "+targetExpr+")";
            if(inner.equals("boolean")||inner.equals("Boolean")) return "BufUtil.ReadBoolArrayInto("+readerVar+", "+targetExpr+")";
            if(inner.equals("char")||inner.equals("Character")) return "BufUtil.ReadCharArrayInto("+readerVar+", "+targetExpr+")";
            if(inner.equals("float")||inner.equals("Float")) return "BufUtil.ReadFloatArrayInto("+readerVar+", "+targetExpr+")";
            if(inner.equals("double")||inner.equals("Double")) return "BufUtil.ReadDoubleArrayInto("+readerVar+", "+targetExpr+")";
            throw new IllegalArgumentException("unsupported primitive array reuse type: "+t);
        }
        static void appendCsWriteStatements(StringBuilder sb, String valueExpr, String t, String writerVar, String indent, boolean hot){
            if(Codegen.isStructType(t) && Codegen.isInlineStructType(t)){
                appendCsInlineWriteStructValue(sb, valueExpr, Codegen.structDef(t), writerVar, indent, hot);
                return;
            }
            if(Codegen.isHotExpandedType(t)){
                appendCsHotWriteValue(sb, valueExpr, t, writerVar, indent, true);
                return;
            }
            sb.append(indent).append(csWriteStmt(valueExpr, t, writerVar)).append(";\n");
        }
        static void appendCsHotReadValue(StringBuilder sb, String targetExpr, String t, String readerVar, String indent, boolean allowNestedHot){
            if(Codegen.isHotObjectArrayType(t)){
                String inner=t.substring(0, t.length()-2).trim();
                String countVar=Codegen.childVar(targetExpr, "count");
                String tmpVar=Codegen.childVar(targetExpr, "tmp");
                String indexVar=Codegen.childVar(targetExpr, "index");
                sb.append(indent).append("{\n");
                sb.append(indent).append("  int ").append(countVar).append("=BufUtil.ReadSize(").append(readerVar).append(");\n");
                sb.append(indent).append("  ").append(csMapType(t)).append(" ").append(tmpVar).append("=new ").append(csMapType(inner)).append("[").append(countVar).append("];\n");
                sb.append(indent).append("  for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendCsAssignReadValue(sb, tmpVar+"["+indexVar+"]", inner, readerVar, indent+"    ", allowNestedHot);
                sb.append(indent).append("  }\n");
                sb.append(indent).append("  ").append(targetExpr).append("=").append(tmpVar).append(";\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(Codegen.isListLikeType(t)){
                String inner=Codegen.genericBody(t).trim();
                String listType=csMapType(t);
                String countVar=Codegen.childVar(targetExpr, "count");
                String tmpVar=Codegen.childVar(targetExpr, "tmp");
                String indexVar=Codegen.childVar(targetExpr, "index");
                String elemVar=Codegen.childVar(targetExpr, "elem");
                sb.append(indent).append("{\n");
                sb.append(indent).append("  int ").append(countVar).append("=BufUtil.ReadSize(").append(readerVar).append(");\n");
                if("LinkedList".equals(Codegen.canonicalContainerType(t))){
                    sb.append(indent).append("  ").append(listType).append(" ").append(tmpVar).append("=new LinkedList<").append(csMapType(inner)).append(">();\n");
                }else{
                    sb.append(indent).append("  ").append(listType).append(" ").append(tmpVar).append("=").append(csBorrowCollectionExpr(t, countVar)).append(";\n");
                }
                sb.append(indent).append("  for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendCsReadValueToLocal(sb, elemVar, inner, readerVar, indent+"    ", allowNestedHot);
                if("LinkedList".equals(Codegen.canonicalContainerType(t))){
                    sb.append(indent).append("    ").append(tmpVar).append(".AddLast(").append(elemVar).append(");\n");
                }else{
                    sb.append(indent).append("    ").append(tmpVar).append(".Add(").append(elemVar).append(");\n");
                }
                sb.append(indent).append("  }\n");
                sb.append(indent).append("  ").append(targetExpr).append("=").append(tmpVar).append(";\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(Codegen.isQueueLikeType(t)){
                String inner=Codegen.genericBody(t).trim();
                String countVar=Codegen.childVar(targetExpr, "count");
                String tmpVar=Codegen.childVar(targetExpr, "tmp");
                String indexVar=Codegen.childVar(targetExpr, "index");
                String elemVar=Codegen.childVar(targetExpr, "elem");
                sb.append(indent).append("{\n");
                sb.append(indent).append("  int ").append(countVar).append("=BufUtil.ReadSize(").append(readerVar).append(");\n");
                sb.append(indent).append("  ").append(csMapType(t)).append(" ").append(tmpVar).append("=").append(csBorrowCollectionExpr(t, countVar)).append(";\n");
                sb.append(indent).append("  for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendCsReadValueToLocal(sb, elemVar, inner, readerVar, indent+"    ", allowNestedHot);
                sb.append(indent).append("    ").append(tmpVar).append(".Add(").append(elemVar).append(");\n");
                sb.append(indent).append("  }\n");
                sb.append(indent).append("  ").append(targetExpr).append("=").append(tmpVar).append(";\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(Codegen.isSetLikeType(t)){
                String inner=Codegen.genericBody(t).trim();
                String countVar=Codegen.childVar(targetExpr, "count");
                String tmpVar=Codegen.childVar(targetExpr, "tmp");
                String indexVar=Codegen.childVar(targetExpr, "index");
                String elemVar=Codegen.childVar(targetExpr, "elem");
                sb.append(indent).append("{\n");
                sb.append(indent).append("  int ").append(countVar).append("=BufUtil.ReadSize(").append(readerVar).append(");\n");
                sb.append(indent).append("  ").append(csMapType(t)).append(" ").append(tmpVar).append("=").append(csBorrowCollectionExpr(t, countVar)).append(";\n");
                sb.append(indent).append("  for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendCsReadValueToLocal(sb, elemVar, inner, readerVar, indent+"    ", allowNestedHot);
                sb.append(indent).append("    ").append(tmpVar).append(".Add(").append(elemVar).append(");\n");
                sb.append(indent).append("  }\n");
                sb.append(indent).append("  ").append(targetExpr).append("=").append(tmpVar).append(";\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(Codegen.isMapLikeType(t)){
                List<String> kv=Codegen.splitTopLevel(Codegen.genericBody(t), ',');
                String keyType=kv.get(0).trim();
                String valueType=kv.get(1).trim();
                String countVar=Codegen.childVar(targetExpr, "count");
                String tmpVar=Codegen.childVar(targetExpr, "tmp");
                String indexVar=Codegen.childVar(targetExpr, "index");
                String keyVar=Codegen.childVar(targetExpr, "key");
                String valueVar=Codegen.childVar(targetExpr, "value");
                sb.append(indent).append("{\n");
                sb.append(indent).append("  int ").append(countVar).append("=BufUtil.ReadSize(").append(readerVar).append(");\n");
                sb.append(indent).append("  ").append(csMapType(t)).append(" ").append(tmpVar).append("=").append(csBorrowCollectionExpr(t, countVar)).append(";\n");
                sb.append(indent).append("  for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendCsReadValueToLocal(sb, keyVar, keyType, readerVar, indent+"    ", allowNestedHot);
                appendCsReadValueToLocal(sb, valueVar, valueType, readerVar, indent+"    ", allowNestedHot);
                sb.append(indent).append("    ").append(tmpVar).append(".Add(").append(keyVar).append(", ").append(valueVar).append(");\n");
                sb.append(indent).append("  }\n");
                sb.append(indent).append("  ").append(targetExpr).append("=").append(tmpVar).append(";\n");
                sb.append(indent).append("}\n");
                return;
            }
            sb.append(indent).append(targetExpr).append("=").append(csReadExpr(t, readerVar)).append(";\n");
        }
        static void appendCsHotWriteValue(StringBuilder sb, String valueExpr, String t, String writerVar, String indent, boolean allowNestedHot){
            if(Codegen.isHotObjectArrayType(t)){
                String inner=t.substring(0, t.length()-2).trim();
                String arrayVar=Codegen.childVar(valueExpr, "array");
                String countVar=Codegen.childVar(valueExpr, "count");
                String indexVar=Codegen.childVar(valueExpr, "index");
                sb.append(indent).append("{\n");
                sb.append(indent).append("  ").append(csMapType(t)).append(" ").append(arrayVar).append("=").append(valueExpr).append(";\n");
                sb.append(indent).append("  int ").append(countVar).append("=").append(arrayVar).append("==null?0:").append(arrayVar).append(".Length;\n");
                sb.append(indent).append("  BufUtil.WriteSize(").append(writerVar).append(", ").append(countVar).append(");\n");
                sb.append(indent).append("  if(").append(countVar).append("!=0 && ").append(arrayVar).append("!=null){\n");
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendCsWriteStatements(sb, arrayVar+"["+indexVar+"]", inner, writerVar, indent+"      ", allowNestedHot);
                sb.append(indent).append("    }\n");
                sb.append(indent).append("  }\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(Codegen.isListLikeType(t)){
                String inner=Codegen.genericBody(t).trim();
                String canonical=Codegen.canonicalContainerType(t);
                String listVar=Codegen.childVar(valueExpr, "list");
                String countVar=Codegen.childVar(valueExpr, "count");
                String spanVar=Codegen.childVar(valueExpr, "span");
                String indexVar=Codegen.childVar(valueExpr, "index");
                String elemVar=Codegen.childVar(valueExpr, "elem");
                sb.append(indent).append("{\n");
                sb.append(indent).append("  ").append(csMapType(t)).append(" ").append(listVar).append("=").append(valueExpr).append(";\n");
                sb.append(indent).append("  int ").append(countVar).append("=").append(listVar).append("==null?0:").append(listVar).append(".Count;\n");
                sb.append(indent).append("  BufUtil.WriteSize(").append(writerVar).append(", ").append(countVar).append(");\n");
                sb.append(indent).append("  if(").append(countVar).append("!=0 && ").append(listVar).append("!=null){\n");
                if("LinkedList".equals(canonical)){
                    sb.append(indent).append("    foreach(var ").append(elemVar).append(" in ").append(listVar).append("){\n");
                    appendCsWriteStatements(sb, elemVar, inner, writerVar, indent+"      ", allowNestedHot);
                    sb.append(indent).append("    }\n");
                }else{
                    sb.append(indent).append("    var ").append(spanVar).append("=CollectionsMarshal.AsSpan(").append(listVar).append(");\n");
                    sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                    appendCsWriteStatements(sb, spanVar+"["+indexVar+"]", inner, writerVar, indent+"      ", allowNestedHot);
                    sb.append(indent).append("    }\n");
                }
                sb.append(indent).append("  }\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(Codegen.isQueueLikeType(t)){
                String inner=Codegen.genericBody(t).trim();
                String queueVar=Codegen.childVar(valueExpr, "queue");
                String countVar=Codegen.childVar(valueExpr, "count");
                String spanVar=Codegen.childVar(valueExpr, "span");
                String indexVar=Codegen.childVar(valueExpr, "index");
                sb.append(indent).append("{\n");
                sb.append(indent).append("  ").append(csMapType(t)).append(" ").append(queueVar).append("=").append(valueExpr).append(";\n");
                sb.append(indent).append("  int ").append(countVar).append("=").append(queueVar).append("==null?0:").append(queueVar).append(".Count;\n");
                sb.append(indent).append("  BufUtil.WriteSize(").append(writerVar).append(", ").append(countVar).append(");\n");
                sb.append(indent).append("  if(").append(countVar).append("!=0 && ").append(queueVar).append("!=null){\n");
                sb.append(indent).append("    var ").append(spanVar).append("=CollectionsMarshal.AsSpan(").append(queueVar).append(");\n");
                sb.append(indent).append("    for(int ").append(indexVar).append("=0;").append(indexVar).append("<").append(countVar).append(";").append(indexVar).append("++){\n");
                appendCsWriteStatements(sb, spanVar+"["+indexVar+"]", inner, writerVar, indent+"      ", allowNestedHot);
                sb.append(indent).append("    }\n");
                sb.append(indent).append("  }\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(Codegen.isSetLikeType(t)){
                String inner=Codegen.genericBody(t).trim();
                String setVar=Codegen.childVar(valueExpr, "set");
                String countVar=Codegen.childVar(valueExpr, "count");
                String elemVar=Codegen.childVar(valueExpr, "elem");
                sb.append(indent).append("{\n");
                sb.append(indent).append("  ").append(csMapType(t)).append(" ").append(setVar).append("=").append(valueExpr).append(";\n");
                sb.append(indent).append("  int ").append(countVar).append("=").append(setVar).append("==null?0:").append(setVar).append(".Count;\n");
                sb.append(indent).append("  BufUtil.WriteSize(").append(writerVar).append(", ").append(countVar).append(");\n");
                sb.append(indent).append("  if(").append(countVar).append("!=0 && ").append(setVar).append("!=null){\n");
                sb.append(indent).append("    foreach(var ").append(elemVar).append(" in ").append(setVar).append("){\n");
                appendCsWriteStatements(sb, elemVar, inner, writerVar, indent+"      ", allowNestedHot);
                sb.append(indent).append("    }\n");
                sb.append(indent).append("  }\n");
                sb.append(indent).append("}\n");
                return;
            }
            if(Codegen.isMapLikeType(t)){
                List<String> kv=Codegen.splitTopLevel(Codegen.genericBody(t), ',');
                String keyType=kv.get(0).trim();
                String valueType=kv.get(1).trim();
                String mapVar=Codegen.childVar(valueExpr, "map");
                String countVar=Codegen.childVar(valueExpr, "count");
                String entryVar=Codegen.childVar(valueExpr, "entry");
                sb.append(indent).append("{\n");
                sb.append(indent).append("  ").append(csMapType(t)).append(" ").append(mapVar).append("=").append(valueExpr).append(";\n");
                sb.append(indent).append("  int ").append(countVar).append("=").append(mapVar).append("==null?0:").append(mapVar).append(".Count;\n");
                sb.append(indent).append("  BufUtil.WriteSize(").append(writerVar).append(", ").append(countVar).append(");\n");
                sb.append(indent).append("  if(").append(countVar).append("!=0 && ").append(mapVar).append("!=null){\n");
                sb.append(indent).append("    foreach(var ").append(entryVar).append(" in ").append(mapVar).append("){\n");
                appendCsWriteStatements(sb, entryVar+".Key", keyType, writerVar, indent+"      ", allowNestedHot);
                appendCsWriteStatements(sb, entryVar+".Value", valueType, writerVar, indent+"      ", allowNestedHot);
                sb.append(indent).append("    }\n");
                sb.append(indent).append("  }\n");
                sb.append(indent).append("}\n");
                return;
            }
            sb.append(indent).append(csWriteStmt(valueExpr, t, writerVar)).append(";\n");
        }
        static void appendCsWriteStatements(StringBuilder sb, String valueExpr, Field f, String writerVar, String indent, boolean hot){
            if(Codegen.isPackedPrimitiveListField(f) || Codegen.isPackedPrimitiveMapField(f)){
                sb.append(indent).append(csWriteStmt(valueExpr, f, writerVar)).append(";\n");
                return;
            }
            appendCsWriteStatements(sb, valueExpr, f.type, writerVar, indent, hot);
        }
        static String csReadExpr(String t){
            return csReadExpr(t, "r");
        }
        static String csReadExpr(Field f, String readerVar){
            if(Codegen.isPackedPrimitiveListField(f)){
                String inner=Codegen.genericBody(f.type).trim();
                if(Codegen.isIntLikeType(inner)) return "BufUtil.ReadPackedIntList("+readerVar+")";
                if(Codegen.isLongLikeType(inner)) return "BufUtil.ReadPackedLongList("+readerVar+")";
                throw new IllegalArgumentException("unsupported C# packed list type: "+f.type);
            }
            if(Codegen.isPackedPrimitiveMapField(f)){
                List<String> kv=Codegen.splitTopLevel(Codegen.genericBody(f.type), ',');
                String keyType=kv.get(0).trim();
                String valueType=kv.get(1).trim();
                if(Codegen.isIntLikeType(keyType) && Codegen.isIntLikeType(valueType)) return "BufUtil.ReadPackedIntIntMap("+readerVar+")";
                if(Codegen.isIntLikeType(keyType) && Codegen.isLongLikeType(valueType)) return "BufUtil.ReadPackedIntLongMap("+readerVar+")";
                throw new IllegalArgumentException("unsupported C# packed map type: "+f.type);
            }
            return csReadExpr(f.type, readerVar);
        }
        static String csReadExpr(Field f, String readerVar, String reuseExpr){
            if(Codegen.isPackedPrimitiveListField(f)){
                String inner=Codegen.genericBody(f.type).trim();
                if(Codegen.isIntLikeType(inner)) return "BufUtil.ReadPackedIntList("+readerVar+", "+reuseExpr+")";
                if(Codegen.isLongLikeType(inner)) return "BufUtil.ReadPackedLongList("+readerVar+", "+reuseExpr+")";
                throw new IllegalArgumentException("unsupported C# packed list type: "+f.type);
            }
            if(Codegen.isPackedPrimitiveMapField(f)){
                List<String> kv=Codegen.splitTopLevel(Codegen.genericBody(f.type), ',');
                String keyType=kv.get(0).trim();
                String valueType=kv.get(1).trim();
                if(Codegen.isIntLikeType(keyType) && Codegen.isIntLikeType(valueType)) return "BufUtil.ReadPackedIntIntMap("+readerVar+", "+reuseExpr+")";
                if(Codegen.isIntLikeType(keyType) && Codegen.isLongLikeType(valueType)) return "BufUtil.ReadPackedIntLongMap("+readerVar+", "+reuseExpr+")";
                throw new IllegalArgumentException("unsupported C# packed map type: "+f.type);
            }
            return csReadExpr(f.type, readerVar);
        }
        static String csReadFixedExpr(String t, String readerVar){
            if(t.equals("int")||t.equals("Integer")) return "BufUtil.ReadFixedInt("+readerVar+")";
            if(t.equals("long")||t.equals("Long")) return "BufUtil.ReadFixedLong("+readerVar+")";
            if(t.equals("byte")||t.equals("Byte")) return "BufUtil.ReadByte("+readerVar+")";
            if(t.equals("short")||t.equals("Short")) return "BufUtil.ReadFixedShort("+readerVar+")";
            if(t.equals("boolean")||t.equals("Boolean")) return "BufUtil.ReadBool("+readerVar+")";
            if(t.equals("char")||t.equals("Character")) return "BufUtil.ReadFixedChar("+readerVar+")";
            if(t.equals("float")||t.equals("Float")) return "BufUtil.ReadFloat("+readerVar+")";
            if(t.equals("double")||t.equals("Double")) return "BufUtil.ReadDouble("+readerVar+")";
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2).trim();
                if(inner.equals("int")||inner.equals("Integer")) return "BufUtil.ReadFixedIntArray("+readerVar+")";
                if(inner.equals("long")||inner.equals("Long")) return "BufUtil.ReadFixedLongArray("+readerVar+")";
                if(inner.equals("byte")||inner.equals("Byte")) return "BufUtil.ReadBytes("+readerVar+")";
                if(inner.equals("short")||inner.equals("Short")) return "BufUtil.ReadFixedShortArray("+readerVar+")";
                if(inner.equals("boolean")||inner.equals("Boolean")) return "BufUtil.ReadBoolArray("+readerVar+")";
                if(inner.equals("char")||inner.equals("Character")) return "BufUtil.ReadFixedCharArray("+readerVar+")";
                if(inner.equals("float")||inner.equals("Float")) return "BufUtil.ReadFixedFloatArray("+readerVar+")";
                if(inner.equals("double")||inner.equals("Double")) return "BufUtil.ReadFixedDoubleArray("+readerVar+")";
                return "BufUtil.ReadObjectArray("+readerVar+", rr=>"+csReadFixedExpr(inner, "rr")+")";
            }
            if(Codegen.ENUMS.contains(t)) return "("+t+")BufUtil.ReadFixedInt("+readerVar+")";
            return t+".ReadFrom("+readerVar+")";
        }
        static String csReadExpr(String t, String readerVar){
            if(t.equals("int")||t.equals("Integer")) return "BufUtil.ReadInt("+readerVar+")";
            if(t.equals("long")||t.equals("Long")) return "BufUtil.ReadLong("+readerVar+")";
            if(t.equals("byte")||t.equals("Byte")) return "BufUtil.ReadByte("+readerVar+")";
            if(t.equals("short")||t.equals("Short")) return "BufUtil.ReadShort("+readerVar+")";
            if(t.equals("boolean")||t.equals("Boolean")) return "BufUtil.ReadBool("+readerVar+")";
            if(t.equals("char")||t.equals("Character")) return "BufUtil.ReadChar("+readerVar+")";
            if(t.equals("float")||t.equals("Float")) return "BufUtil.ReadFloat("+readerVar+")";
            if(t.equals("double")||t.equals("Double")) return "BufUtil.ReadDouble("+readerVar+")";
            if(t.equals("String")||t.equals("string")) return "BufUtil.ReadString("+readerVar+")";
            if(Codegen.ENUMS.contains(t)) return "("+t+")BufUtil.ReadUInt("+readerVar+")";
            if(Codegen.isOptionalType(t)){
                String inner=Codegen.genericBody(t).trim();
                if(isCsValueType(inner)){
                    return "BufUtil.ReadOptionalValue<"+csMapType(inner)+">("+readerVar+", rr=>"+csReadExpr(inner, "rr")+")";
                }
                return "BufUtil.ReadOptionalRef<"+csMapType(inner)+">("+readerVar+", rr=>"+csReadExpr(inner, "rr")+")";
            }
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2);
                if(inner.equals("int")||inner.equals("Integer")) return "BufUtil.ReadIntArray("+readerVar+")";
                if(inner.equals("long")||inner.equals("Long")) return "BufUtil.ReadLongArray("+readerVar+")";
                if(inner.equals("byte")||inner.equals("Byte")) return "BufUtil.ReadBytes("+readerVar+")";
                if(inner.equals("short")||inner.equals("Short")) return "BufUtil.ReadShortArray("+readerVar+")";
                if(inner.equals("boolean")||inner.equals("Boolean")) return "BufUtil.ReadBoolArray("+readerVar+")";
                if(inner.equals("char")||inner.equals("Character")) return "BufUtil.ReadCharArray("+readerVar+")";
                if(inner.equals("float")||inner.equals("Float")) return "BufUtil.ReadFloatArray("+readerVar+")";
                if(inner.equals("double")||inner.equals("Double")) return "BufUtil.ReadDoubleArray("+readerVar+")";
                return "BufUtil.ReadObjectArray("+readerVar+", rr=>"+csReadExpr(inner, "rr")+")";
            }
            if(Codegen.isListLikeType(t)){
                String inner=Codegen.genericBody(t).trim();
                String listExpr="BufUtil.ReadList("+readerVar+", rr=>"+csReadExpr(inner, "rr")+")";
                if("LinkedList".equals(Codegen.canonicalContainerType(t))){
                    return "BufUtil.ReadCollection("+readerVar+", _=>new LinkedList<"+csMapType(inner)+">(), rr=>"+csReadExpr(inner, "rr")+")";
                }
                return listExpr;
            }
            if(Codegen.isSetLikeType(t)){
                String inner=Codegen.genericBody(t).trim();
                return "BufUtil.ReadSet("+readerVar+", rr=>"+csReadExpr(inner, "rr")+")";
            }
            if(Codegen.isQueueLikeType(t)) return "BufUtil.ReadList("+readerVar+", rr=>"+csReadExpr(Codegen.genericBody(t).trim(), "rr")+")";
            if(Codegen.isMapLikeType(t)){
                String inside=Codegen.genericBody(t);
                List<String> kv=Codegen.splitTopLevel(inside, ',');
                String kt=kv.get(0).trim();
                String vt=kv.get(1).trim();
                return "BufUtil.ReadMap("+readerVar+", rr=>"+csReadExpr(kt, "rr")+", rr=>"+csReadExpr(vt, "rr")+")";
            }
            return t+".ReadFrom("+readerVar+")";
        }
        static String csWriteStmt(String var,String t){
            return csWriteStmt(var, t, "w");
        }
        static String csWriteStmt(String var, Field f, String writerVar){
            if(Codegen.isPackedPrimitiveListField(f)){
                String inner=Codegen.genericBody(f.type).trim();
                if(Codegen.isIntLikeType(inner)) return "BufUtil.WritePackedIntList("+writerVar+","+var+")";
                if(Codegen.isLongLikeType(inner)) return "BufUtil.WritePackedLongList("+writerVar+","+var+")";
                throw new IllegalArgumentException("unsupported C# packed list type: "+f.type);
            }
            if(Codegen.isPackedPrimitiveMapField(f)){
                List<String> kv=Codegen.splitTopLevel(Codegen.genericBody(f.type), ',');
                String keyType=kv.get(0).trim();
                String valueType=kv.get(1).trim();
                if(Codegen.isIntLikeType(keyType) && Codegen.isIntLikeType(valueType)) return "BufUtil.WritePackedIntIntMap("+writerVar+","+var+")";
                if(Codegen.isIntLikeType(keyType) && Codegen.isLongLikeType(valueType)) return "BufUtil.WritePackedIntLongMap("+writerVar+","+var+")";
                throw new IllegalArgumentException("unsupported C# packed map type: "+f.type);
            }
            return csWriteStmt(var, f.type, writerVar);
        }
        static String csWriteFixedStmt(String var, String t, String writerVar){
            if(t.equals("int")||t.equals("Integer")) return "BufUtil.WriteFixedInt("+writerVar+","+var+")";
            if(t.equals("long")||t.equals("Long")) return "BufUtil.WriteFixedLong("+writerVar+","+var+")";
            if(t.equals("byte")||t.equals("Byte")) return "BufUtil.WriteByte("+writerVar+","+var+")";
            if(t.equals("short")||t.equals("Short")) return "BufUtil.WriteFixedShort("+writerVar+","+var+")";
            if(t.equals("boolean")||t.equals("Boolean")) return "BufUtil.WriteBool("+writerVar+","+var+")";
            if(t.equals("char")||t.equals("Character")) return "BufUtil.WriteFixedChar("+writerVar+","+var+")";
            if(t.equals("float")||t.equals("Float")) return "BufUtil.WriteFloat("+writerVar+","+var+")";
            if(t.equals("double")||t.equals("Double")) return "BufUtil.WriteDouble("+writerVar+","+var+")";
            if(Codegen.ENUMS.contains(t)) return "BufUtil.WriteFixedInt("+writerVar+",(int)"+var+")";
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2).trim();
                if(inner.equals("int")||inner.equals("Integer")) return "BufUtil.WriteFixedIntArray("+writerVar+","+var+")";
                if(inner.equals("long")||inner.equals("Long")) return "BufUtil.WriteFixedLongArray("+writerVar+","+var+")";
                if(inner.equals("byte")||inner.equals("Byte")) return "BufUtil.WriteBytes("+writerVar+","+var+")";
                if(inner.equals("short")||inner.equals("Short")) return "BufUtil.WriteFixedShortArray("+writerVar+","+var+")";
                if(inner.equals("boolean")||inner.equals("Boolean")) return "BufUtil.WriteBoolArray("+writerVar+","+var+")";
                if(inner.equals("char")||inner.equals("Character")) return "BufUtil.WriteFixedCharArray("+writerVar+","+var+")";
                if(inner.equals("float")||inner.equals("Float")) return "BufUtil.WriteFixedFloatArray("+writerVar+","+var+")";
                if(inner.equals("double")||inner.equals("Double")) return "BufUtil.WriteFixedDoubleArray("+writerVar+","+var+")";
                return "BufUtil.WriteObjectArray("+writerVar+","+var+", (ww,x)=>"+csWriteFixedStmt("x", inner, "ww")+")";
            }
            return var+".WriteTo("+writerVar+")";
        }
        static void appendCsPresenceWritePrelude(StringBuilder sb, List<Field> optionalFields, String valuePrefix, String writerVar, String indent){
            appendCsPresenceWritePrelude(sb, optionalFields, valuePrefix, writerVar, indent, "__presence");
        }
        static void appendCsPresenceWritePrelude(StringBuilder sb, List<Field> optionalFields, String valuePrefix, String writerVar, String indent, String presenceVar){
            if(optionalFields.isEmpty()) return;
            int optionalCount=optionalFields.size();
            for(Field field: optionalFields){
                String fieldExpr=valuePrefix+Codegen.cap(field.name);
                String valueVar=csPresenceValueVar(field);
                String hasVar=csPresenceHasVar(field);
                sb.append(indent).append(csWriteCacheType(field.type)).append(" ").append(valueVar).append("=").append(fieldExpr).append(";\n");
                sb.append(indent).append("bool ").append(hasVar).append("=").append(csHasWireValueExpr(valueVar, field.type)).append(";\n");
            }
            if(Codegen.useSinglePresenceWord(optionalCount)){
                sb.append(indent).append("ulong ").append(presenceVar).append("=0UL;\n");
                for(int i=0;i<optionalCount;i++){
                    Field field=optionalFields.get(i);
                    sb.append(indent).append("if(").append(csPresenceHasVar(field)).append(") ").append(presenceVar).append(" |= 1UL << ").append(i).append(";\n");
                }
                sb.append(indent).append("BufUtil.WritePresenceBits(").append(writerVar).append(", ").append(presenceVar).append(", ").append(optionalCount).append(");\n");
                return;
            }
            sb.append(indent).append("ulong[] ").append(presenceVar).append("=new ulong[").append((optionalCount+63)>>>6).append("];\n");
            for(int i=0;i<optionalCount;i++){
                Field field=optionalFields.get(i);
                sb.append(indent).append("if(").append(csPresenceHasVar(field)).append(") ").append(presenceVar).append("[")
                        .append(i>>>6).append("] |= 1UL << ").append(i&63).append(";\n");
            }
            sb.append(indent).append("BufUtil.WritePresenceBits(").append(writerVar).append(", ").append(presenceVar).append(", ").append(optionalCount).append(");\n");
        }
        static void appendCsPresenceReadPrelude(StringBuilder sb, int optionalCount, String readerVar, String indent){
            appendCsPresenceReadPrelude(sb, optionalCount, readerVar, indent, "__presence");
        }
        static void appendCsPresenceReadPrelude(StringBuilder sb, int optionalCount, String readerVar, String indent, String presenceVar){
            if(optionalCount==0) return;
            if(Codegen.useSinglePresenceWord(optionalCount)){
                sb.append(indent).append("ulong ").append(presenceVar).append("=BufUtil.ReadPresenceBits(").append(readerVar).append(", ").append(optionalCount).append(");\n");
            }else{
                sb.append(indent).append("ulong[] ").append(presenceVar).append("=BufUtil.ReadPresenceWords(").append(readerVar).append(", ").append(optionalCount).append(");\n");
            }
        }
        static String csPresenceExpr(String presenceVar, int bitIndex, int optionalCount){
            if(Codegen.useSinglePresenceWord(optionalCount)){
                return "(("+presenceVar+" & (1UL << "+bitIndex+")) != 0UL)";
            }
            return "BufUtil.IsPresenceBitSet("+presenceVar+", "+bitIndex+")";
        }
        static String csOptionalHasValueExpr(String var){
            return var+" != null";
        }
        static String csOptionalValueExpr(String var, String innerType){
            return isCsValueType(innerType)? var+".Value" : var;
        }
        static String csWriteStmt(String var,String t, String writerVar){
            if(t.equals("int")||t.equals("Integer")) return "BufUtil.WriteInt("+writerVar+","+var+")";
            if(t.equals("long")||t.equals("Long")) return "BufUtil.WriteLong("+writerVar+","+var+")";
            if(t.equals("byte")||t.equals("Byte")) return "BufUtil.WriteByte("+writerVar+","+var+")";
            if(t.equals("short")||t.equals("Short")) return "BufUtil.WriteShort("+writerVar+","+var+")";
            if(t.equals("boolean")||t.equals("Boolean")) return "BufUtil.WriteBool("+writerVar+","+var+")";
            if(t.equals("char")||t.equals("Character")) return "BufUtil.WriteChar("+writerVar+","+var+")";
            if(t.equals("float")||t.equals("Float")) return "BufUtil.WriteFloat("+writerVar+","+var+")";
            if(t.equals("double")||t.equals("Double")) return "BufUtil.WriteDouble("+writerVar+","+var+")";
            if(t.equals("String")||t.equals("string")) return "BufUtil.WriteString("+writerVar+","+var+")";
            if(Codegen.ENUMS.contains(t)) return "BufUtil.WriteUInt("+writerVar+",(int)"+var+")";
            if(Codegen.isOptionalType(t)){
                String inner=Codegen.genericBody(t).trim();
                return "BufUtil.WriteOptional("+writerVar+","+var+", (ww,v)=>"+csWriteStmt("v", inner, "ww")+")";
            }
            if(t.endsWith("[]")){
                String inner=t.substring(0,t.length()-2);
                if(inner.equals("int")||inner.equals("Integer")) return "BufUtil.WriteIntArray("+writerVar+","+var+")";
                if(inner.equals("long")||inner.equals("Long")) return "BufUtil.WriteLongArray("+writerVar+","+var+")";
                if(inner.equals("byte")||inner.equals("Byte")) return "BufUtil.WriteBytes("+writerVar+","+var+")";
                if(inner.equals("short")||inner.equals("Short")) return "BufUtil.WriteShortArray("+writerVar+","+var+")";
                if(inner.equals("boolean")||inner.equals("Boolean")) return "BufUtil.WriteBoolArray("+writerVar+","+var+")";
                if(inner.equals("char")||inner.equals("Character")) return "BufUtil.WriteCharArray("+writerVar+","+var+")";
                if(inner.equals("float")||inner.equals("Float")) return "BufUtil.WriteFloatArray("+writerVar+","+var+")";
                if(inner.equals("double")||inner.equals("Double")) return "BufUtil.WriteDoubleArray("+writerVar+","+var+")";
                return "BufUtil.WriteObjectArray("+writerVar+","+var+", (ww,x)=>"+csWriteStmt("x", inner, "ww")+")";
            }
            if(Codegen.isListLikeType(t) || Codegen.isSetLikeType(t) || Codegen.isQueueLikeType(t)){
                String inner=Codegen.genericBody(t).trim();
                return "BufUtil.WriteCollection("+writerVar+","+var+", (ww,x)=>"+csWriteStmt("x", inner, "ww")+")";
            }
            if(Codegen.isMapLikeType(t)){
                String inside=Codegen.genericBody(t);
                List<String> kv=Codegen.splitTopLevel(inside, ',');
                String kt=kv.get(0).trim();
                String vt=kv.get(1).trim();
                return "BufUtil.WriteMap("+writerVar+","+var+", (ww,k)=>"+csWriteStmt("k", kt, "ww")+", (ww,v)=>"+csWriteStmt("v", vt, "ww")+")";
            }
            return var+".WriteTo("+writerVar+")";
        }
        static String generateIds(String ns, List<Assign> assigns){
            StringBuilder sb=new StringBuilder();
            sb.append("namespace ").append(ns).append(" { public static class ProtoIds { \n");
            int max=0;
            for(Assign a: assigns){
                int id=a.c2sStart;
                for(Method m: a.c2s){
                    sb.append("public const int ").append(a.baseCamel.toUpperCase()).append("_").append(m.name.toUpperCase()).append(" = ").append(id).append(";\n");
                    max=Math.max(max,id); id+=2;
                }
                id=a.s2cStart;
                for(Method m: a.s2c){
                    sb.append("public const int ").append(a.baseCamel.toUpperCase()).append("_").append(m.name.toUpperCase()).append(" = ").append(id).append(";\n");
                    max=Math.max(max,id); id+=2;
                }
            }
            sb.append("public const int MAX_ID = ").append(max).append("; } }");
            return sb.toString();
        }
    }
}


