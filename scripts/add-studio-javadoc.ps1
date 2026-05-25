# Adds minimal class-level Javadoc to llw-studio Java files that lack it.
param(
    [string]$Root = "llw-studio/src"
)

function Get-HumanName([string]$typeName) {
    $n = $typeName -replace '([a-z])([A-Z])', '$1 $2'
    return $n.Trim()
}

Get-ChildItem -Path $Root -Recurse -Filter "*.java" | ForEach-Object {
    $path = $_.FullName
    if ($_.Name -eq "package-info.java") { return }

    $text = Get-Content $path -Raw
    if ($text -match '(?s)/\*\*.*?\*/\s*(?:@\w+.*\s*)*(?:public\s+)?(?:final\s+)?(?:class|interface|enum|record)\s+') {
        return
    }

    if ($text -notmatch '(?:public\s+)?(?:final\s+)?(class|interface|enum|record)\s+(\w+)') {
        return
    }

    $kind = $Matches[1]
    $name = $Matches[2]
    $human = Get-HumanName $name
    $isTest = $path -match '\\test\\'

    if ($isTest) {
        $doc = "/**`r`n * Tests $human.`r`n */`r`n"
    } else {
        $doc = "/**`r`n * $human.`r`n */`r`n"
    }

    $newText = $text -replace '((?:public\s+)?(?:final\s+)?(?:class|interface|enum|record)\s+' + [regex]::Escape($name) + ')', ($doc + '$1')
    if ($newText -ne $text) {
        Set-Content -Path $path -Value $newText -NoNewline
        Write-Host "Updated: $($_.Name)"
    }
}
